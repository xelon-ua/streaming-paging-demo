package com.example.streaming_paging_demo.orders

import com.example.streaming_paging_demo.db.DatabaseFactory
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private const val SSE_HEADER_REQUEST_ID: String = "X-SSE-Request-ID"
private const val FILTER_TTL_MS: Long = 5 * 60 * 1000

class SseFilterCache {
    private data class Entry(val rawJson: String, val createdAt: Long)

    private val map = ConcurrentHashMap<String, Entry>()

    fun stage(rawJson: String): String {
        val id = UUID.randomUUID().toString()
        map[id] = Entry(rawJson, System.currentTimeMillis())
        return id
    }

    fun get(id: String): String? {
        val e = map[id] ?: return null
        if (System.currentTimeMillis() - e.createdAt > FILTER_TTL_MS) {
            map.remove(id)
            return null
        }
        return e.rawJson
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

fun Application.configureSse() {
    install(SSE)
}

/**
 * Extracts `OrderFilterDto` from the `X-SSE-Request-ID` header using the provided `SseFilterCache`.
 * Responds with `403 Forbidden` and returns null if the header is missing or the cache entry expired/unknown.
 */
private suspend fun ApplicationCall.extractOrderFilter(cache: SseFilterCache): OrderFilterDto? {
    val requestId = request.headers[SSE_HEADER_REQUEST_ID]
    if (requestId == null) {
        respond(HttpStatusCode.Forbidden, "Missing $SSE_HEADER_REQUEST_ID")
        return null
    }
    val rawFilters = cache.get(requestId)
    if (rawFilters == null) {
        respond(HttpStatusCode.Forbidden, "Expired or unknown requestId")
        return null
    }
    return Json.decodeFromString<OrderFilterDto>(rawFilters)
}

fun Application.registerOrderRoutes() {
    val repo = OrderRepository()
    val cache = SseFilterCache()

    DatabaseFactory.init()
    repo.seedIfEmpty()

    routing {
        route("/orders") {
            post("/sse") {
                val rawFilters = call.receiveText()
                val id = cache.stage(rawFilters)
                call.respond(id)
            }

            route("/sse/count", method = HttpMethod.Get) {
                sse {
                    val filter = call.extractOrderFilter(cache) ?: return@sse
                    repo.countAllFlow(filter).collect { total ->
                        send(total.toString())
                    }
                }
            }

            route("/sse", method = HttpMethod.Get) {
                sse {
                    val filter = call.extractOrderFilter(cache) ?: return@sse
                    val position = call.request.queryParameters["position"]?.toIntOrNull() ?: 0
                    val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 30

                    repo.pageFlow(position, size, filter).collect { items ->
                        val map: Map<Int, OrderRow> = items.mapIndexed { idx, o -> (position + idx) to o }.toMap()
                        val json = Json.encodeToString(
                            MapSerializer(
                                kotlinx.serialization.serializer<Int>(),
                                kotlinx.serialization.serializer<OrderRow>()
                            ),
                            map
                        )
                        send(json)
                    }
                }
            }
        }
    }
}

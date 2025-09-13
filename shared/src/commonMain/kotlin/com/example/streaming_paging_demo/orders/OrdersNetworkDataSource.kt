package com.example.streaming_paging_demo.orders

import co.touchlab.kermit.Logger
import com.example.streaming_paging_demo.SERVER_PORT
import com.example.streaming_paging_demo.network.createHttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeoutConfig
import io.ktor.client.plugins.sse.sse
import io.ktor.client.plugins.timeout
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.takeFrom
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json

private const val SSE_HEADER_REQUEST_ID: String = "X-SSE-Request-ID"

class OrdersNetworkDataSource(
    private val baseUrl: String = "http://localhost:$SERVER_PORT",
) {
    private val client = createHttpClient()
    private val logger = Logger.withTag("OrdersClient")

    suspend fun stageSseFilters(filter: OrderFilter = OrderFilter()): String {
        val id: String = client.post("$baseUrl/orders/sse") {
            contentType(ContentType.Application.Json)
            setBody(filter)
        }.body()
        return id
    }

    fun totalCountFlow(requestId: String): Flow<Long> = channelFlow {
        client.sse({
            url.takeFrom("$baseUrl/orders/sse/count")
            headers.append(SSE_HEADER_REQUEST_ID, requestId)
            timeout {
                connectTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            }
        }) {
            incoming.collect { event ->
                val data = event.data ?: return@collect
                val count = data.toLongOrNull()
                if (count != null) {
                    trySend(count)
                }
            }
        }
    }

    fun ordersPortionFlow(position: Int, size: Int, requestId: String): Flow<Map<Int, Order>> = channelFlow {
        client.sse({
            url.takeFrom("$baseUrl/orders/sse")
            headers.append(SSE_HEADER_REQUEST_ID, requestId)
            parameter("position", position)
            parameter("size", size)
            timeout {
                connectTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                socketTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
                requestTimeoutMillis = HttpTimeoutConfig.INFINITE_TIMEOUT_MS
            }
        }) {
            incoming.collect { event ->
                val json = event.data ?: return@collect
                val map: Map<String, Order> = Json.decodeFromString(json)
                val result = map.mapKeys { it.key.toInt() }
                trySend(result)
            }
        }
    }
}

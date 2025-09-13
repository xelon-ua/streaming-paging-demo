package com.example.streaming_paging_demo

import com.example.streaming_paging_demo.orders.configureSerialization
import com.example.streaming_paging_demo.orders.configureSse
import com.example.streaming_paging_demo.orders.registerOrderRoutes
import com.example.streaming_paging_demo.orders.OrderRepository
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSse()
    registerOrderRoutes()

    scheduleRandomOrderInsertion()

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
    }
}

private fun Application.scheduleRandomOrderInsertion() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val repository = OrderRepository()
    var job: Job? = null

    monitor.subscribe(ApplicationStarted) {
        job = scope.launch {
            while (isActive) {
                try {
                    repository.insertRandomOrder()
                } catch (t: Throwable) {
                    environment.log.error("Failed to insert random order", t)
                }
                delay(5_000L)
            }
        }
    }

    monitor.subscribe(ApplicationStopping) {
        job?.cancel()
        scope.cancel()
    }
}
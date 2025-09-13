package com.example.streaming_paging_demo.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    engine {
        config {
            // Disable timeouts for long-lived SSE connections
            readTimeout(0, TimeUnit.MILLISECONDS)
            writeTimeout(0, TimeUnit.MILLISECONDS)
            callTimeout(0, TimeUnit.MILLISECONDS)
        }
    }
    install(Logging) { level = LogLevel.ALL }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(SSE)
}

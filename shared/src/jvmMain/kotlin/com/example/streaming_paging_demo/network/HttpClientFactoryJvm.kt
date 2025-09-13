package com.example.streaming_paging_demo.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.ALL
    }
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(SSE)
}

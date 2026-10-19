package org.example.data.remote.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.example.data.remote.config.SupabaseConfig
import io.ktor.client.plugins.api.createClientPlugin
import org.example.data.ratelimit.RateLimiter

class SupabaseHttpClient(
    private val config: SupabaseConfig,
    private val rateLimiter: RateLimiter
) {

    val rateLimiterPlugin = createClientPlugin("RateLimiterPlugin") {
        onRequest { _, _ ->
            rateLimiter.acquire()
        }
    }

    fun create(): HttpClient {
        return HttpClient(CIO) {
            expectSuccess = true
            install(rateLimiterPlugin)
            install(HttpTimeout) {
                connectTimeoutMillis = CONNECT_TIMEOUT_MS
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
                socketTimeoutMillis = SOCKET_TIMEOUT_MS
            }
            install(ContentNegotiation) {
                json(Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    encodeDefaults = true
                })
            }
            defaultRequest {
                header("apikey", config.publishableKey)
                header("Authorization", "Bearer ${config.publishableKey}")
                header("Content-Type", "application/json")
            }
        }
    }

    companion object {
        const val CONNECT_TIMEOUT_MS = 5_000L
        const val REQUEST_TIMEOUT_MS = 10_000L
        const val SOCKET_TIMEOUT_MS = 10_000L
    }
}

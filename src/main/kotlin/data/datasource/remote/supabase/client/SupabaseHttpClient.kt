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

class SupabaseHttpClient(
    private val config: SupabaseConfig
) {

    fun create(): HttpClient {

        return HttpClient(CIO) {
            expectSuccess = true
            install(HttpTimeout) {
                connectTimeoutMillis = 5_000
                requestTimeoutMillis = 10_000
                socketTimeoutMillis = 10_000
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
}

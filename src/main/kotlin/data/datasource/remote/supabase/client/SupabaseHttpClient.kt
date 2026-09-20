package org.example.data.remote.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.example.data.remote.config.SupabaseConfig

class SupabaseHttpClient(
    private val config: SupabaseConfig
) {

    fun create(): HttpClient {

        return HttpClient(CIO) {

            install(ContentNegotiation) {

                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }
                )

            }


            defaultRequest {

                header(
                    "apikey",
                    config.publishableKey
                )

                header(
                    "Authorization",
                    "Bearer ${config.publishableKey}"
                )

                header(
                    "Content-Type",
                    "application/json"
                )
            }
        }
    }
}

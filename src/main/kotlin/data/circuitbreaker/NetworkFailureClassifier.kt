package org.example.data.circuitbreaker

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException
import kotlinx.coroutines.CancellationException
import org.example.data.exception.CallNotPermittedException

class NetworkFailureClassifier {

    companion object {
        private const val MAX_CAUSE_DEPTH = 10
    }
    fun isNetworkFailure(error: Throwable): Boolean {
        return generateSequence(error) { it.cause }
            .take(MAX_CAUSE_DEPTH)
            .any { cause ->
                when (cause) {
                    is CancellationException,
                    is CallNotPermittedException -> false

                    is HttpRequestTimeoutException,
                    is ConnectTimeoutException,
                    is SocketTimeoutException,
                    is IOException,
                    is ServerResponseException -> true

                    is ClientRequestException ->
                        cause.response.status ==
                                HttpStatusCode.RequestTimeout ||
                                cause.response.status ==
                                HttpStatusCode.TooManyRequests

                    else -> false
                }
            }
    }
}


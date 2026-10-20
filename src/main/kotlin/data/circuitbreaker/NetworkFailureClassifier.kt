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

    fun isNetworkFailure(error: Throwable): Boolean {
        var current: Throwable? = error
        val visited = mutableSetOf<Throwable>()

        while (current != null && visited.add(current)) {
            if (current is CancellationException ||
                current is CallNotPermittedException
            ) {
                return false
            }

            when (current) {
                is HttpRequestTimeoutException,
                is ConnectTimeoutException,
                is SocketTimeoutException,
                is IOException,
                is ServerResponseException -> return true

                is ClientRequestException -> {
                    return current.response.status ==
                            HttpStatusCode.RequestTimeout ||
                            current.response.status ==
                            HttpStatusCode.TooManyRequests
                }
            }

            current = current.cause
        }

        return false
    }
}


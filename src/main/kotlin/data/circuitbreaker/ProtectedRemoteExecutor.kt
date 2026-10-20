package org.example.data.circuitbreaker

import kotlinx.coroutines.CancellationException
import org.example.data.retry.RetryWithBackoff

class ProtectedRemoteExecutor(
    private val circuitBreaker: CircuitBreaker,
    private val retry: RetryWithBackoff
) {
    suspend fun <T> execute(
        useRetry: Boolean = false,
        operation: suspend () -> T
    ): T {
        return circuitBreaker.execute {
            if (useRetry) {
                retry.executeWithRetry {
                    operation()
                }
            } else {
                runCatching {
                    operation()
                }.onFailure { error ->
                    if (error is CancellationException) {
                        throw error
                    }
                }
            }
        }.getOrThrow()
    }
}


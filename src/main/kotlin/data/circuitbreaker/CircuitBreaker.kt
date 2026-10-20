package org.example.data.circuitbreaker

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.example.data.exception.CallNotPermittedException

class CircuitBreaker(
    private val failureThreshold: Int = DEFAULT_FAILURE_THRESHOLD,
    private val resetTimeoutMillis: Long = DEFAULT_RESET_TIMEOUT_MS,
    private val isNetworkFailure: (Throwable) -> Boolean,
    private val currentTimeNanos: () -> Long = System::nanoTime
) {
    companion object {
        private const val DEFAULT_FAILURE_THRESHOLD = 5
        private const val DEFAULT_RESET_TIMEOUT_MS = 5_000L
    }
    private val mutex = Mutex()
    private var state = CircuitBreakerState.CLOSED
    private var failureCount = 0
    private var openedAtNanos = 0L
    private var probeInProgress = false
    private var generation = 0L

    init {
        require(failureThreshold > 0) { "Failure threshold must be greater than zero" }

        require(resetTimeoutMillis > 0) { "Reset timeout must be greater than zero" }
    }

    suspend fun <T> execute(operation: suspend () -> Result<T>): Result<T> {
        val permission = acquirePermission().getOrElse { error ->
            throw error
        }
        var completed = false

        try {
            val result = operation()
            val error = result.exceptionOrNull()
            if (error is CancellationException) {
                throw error
            }

            withContext(NonCancellable) {
                if (result.isSuccess) {
                    onSuccess(permission)
                } else {
                    onFailure(permission, requireNotNull(error))
                }
            }
            completed = true
            return result
        } finally {
            if (!completed) {
                withContext(NonCancellable) {
                    onCancellation(permission)
                }
            }
        }
    }

    private suspend fun acquirePermission(): Result<Permission> =
        mutex.withLock {
            if (state == CircuitBreakerState.OPEN) {
                val elapsed = currentTimeNanos() - openedAtNanos
                val timeout = TimeUnit.MILLISECONDS.toNanos(resetTimeoutMillis)

                if (elapsed < timeout) {
                    return@withLock Result.failure(CallNotPermittedException())
                }
                state = CircuitBreakerState.HALF_OPEN
            }

            when (state) {
                CircuitBreakerState.CLOSED -> {
                    Result.success(Permission(isProbe = false, generation = generation))
                }

                CircuitBreakerState.HALF_OPEN -> {
                    if (probeInProgress) {
                        Result.failure(CallNotPermittedException())
                    } else {
                        probeInProgress = true
                        Result.success(Permission(isProbe = true, generation = generation))
                    }
                }

                CircuitBreakerState.OPEN -> {
                    Result.failure(CallNotPermittedException())
                }
            }
        }

    private suspend fun onSuccess(permission: Permission) {
        mutex.withLock {
            if (permission.generation != generation) {
                return@withLock
            }

            if (permission.isProbe) {
                state = CircuitBreakerState.CLOSED
                failureCount = 0
                probeInProgress = false
                generation++
            } else if (state == CircuitBreakerState.CLOSED) {
                failureCount = 0
            }
        }
    }

    private suspend fun onFailure(permission: Permission, error: Throwable) {
        val networkFailure = isNetworkFailure(error)
        mutex.withLock {
            if (permission.generation != generation) {
                return@withLock
            }
            if (permission.isProbe) {
                openCircuit()
                return@withLock
            }
            if (state != CircuitBreakerState.CLOSED) {
                return@withLock
            }
            if (networkFailure) {
                failureCount++
                if (failureCount >= failureThreshold) {
                    openCircuit()
                }
            } else {
                failureCount = 0
            }
        }
    }

    private suspend fun onCancellation(permission: Permission) {
        mutex.withLock {
            if (permission.generation != generation) {
                return@withLock
            }
            if (permission.isProbe) {
                openCircuit()
            }
        }
    }

    private fun openCircuit() {
        state = CircuitBreakerState.OPEN
        openedAtNanos = currentTimeNanos()
        failureCount = 0
        probeInProgress = false
        generation++
    }

    private data class Permission(
        val isProbe: Boolean,
        val generation: Long
    )
}


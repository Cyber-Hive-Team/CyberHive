package org.example.data.retry

import kotlinx.coroutines.delay
import org.example.data.mapper.DataExceptionMapper
import org.example.domain.model.exception.NetworkException


class RetryWithBackoff(
    private val maxRetries: Int = DEFAULT_MAX_RETRIES,
    private val initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
    private val factor: Double = DEFAULT_FACTOR,
    private val logger: RetryLogger = NoOpRetryLogger
) {
    companion object {
        const val DEFAULT_MAX_RETRIES = 3
        const val DEFAULT_INITIAL_DELAY_MS = 1000L
        const val DEFAULT_FACTOR = 2.0
        const val ATTEMPT_DISPLAY_OFFSET = 1
    }

    private val exceptionMapper = DataExceptionMapper()

    suspend fun <T> executeWithRetry(block: suspend () -> T): Result<T> {
        var currentDelay = initialDelayMs
            repeat(maxRetries + ATTEMPT_DISPLAY_OFFSET) { attempt ->
                val outcome = runCatching { block() }

                if (!shouldRetry(outcome.exceptionOrNull(), attempt, maxRetries)) {
                    outcome
                        .onSuccess { logger.onSucceeded(it) }
                        .onFailure { logger.onGaveUp(it) }
                    return outcome
                }

                outcome.onFailure { error ->
                    logger.onAttemptFailed(
                        attempt = attempt + ATTEMPT_DISPLAY_OFFSET,
                        error = error,
                        nextDelayMs = currentDelay
                    )
                }

                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }

        error("Retry loop ended unexpectedly")
    }
    private fun shouldRetry(error: Throwable?, attempt: Int, maxRetries: Int): Boolean =
        error != null && isRetryable(error) && attempt < maxRetries

    private fun isRetryable(error: Throwable): Boolean =
        exceptionMapper.map(error) is NetworkException
}

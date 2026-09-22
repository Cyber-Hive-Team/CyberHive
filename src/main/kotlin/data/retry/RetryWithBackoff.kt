package org.example.data.retry

import org.example.data.mapper.DataExceptionMapper
import org.example.domain.model.exception.NetworkException

private const val DEFAULT_MAX_RETRIES = 3
private const val DEFAULT_INITIAL_DELAY_MS = 1000L
private const val DEFAULT_FACTOR = 2.0
private const val ATTEMPT_DISPLAY_OFFSET = 1

private val exceptionMapper = DataExceptionMapper()

fun <T> retryWithBackoff(
    maxRetries: Int = DEFAULT_MAX_RETRIES,
    initialDelayMs: Long = DEFAULT_INITIAL_DELAY_MS,
    factor: Double = DEFAULT_FACTOR,
    block: () -> T
): Result<T> {
    var currentDelay = initialDelayMs
    var outcome: Result<T> = Result.failure(IllegalStateException("retryWithBackoff did not run"))

    for (attempt in 0..maxRetries) {
        outcome = runCatching { block() }

        if (!shouldRetry(outcome.exceptionOrNull(), attempt, maxRetries)) break

        outcome.onFailure { error ->
            println(
                "Attempt ${attempt + ATTEMPT_DISPLAY_OFFSET} failed: ${error::class.simpleName}. " +
                        "Retrying in ${currentDelay}ms"
            )
        }
        Thread.sleep(currentDelay)
        currentDelay = (currentDelay * factor).toLong()
    }

    outcome
        .onSuccess { println("Succeeded with result: $it") }
        .onFailure { println("Gave up: ${it::class.simpleName} - ${it.message}") }

    return outcome
}

private fun shouldRetry(error: Throwable?, attempt: Int, maxRetries: Int): Boolean =
    error != null && isRetryable(error) && attempt < maxRetries

private fun isRetryable(error: Throwable): Boolean =
    exceptionMapper.map(error) is NetworkException

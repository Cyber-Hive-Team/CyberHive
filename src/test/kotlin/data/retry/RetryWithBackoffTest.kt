
package org.example.test.data.retry

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.example.data.retry.RetryWithBackoff
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RetryWithBackoffTest {

    private val retry = RetryWithBackoff(
        maxRetries = 3,
        initialDelayMs = 1000L,
        factor = 2.0
    )

    @Test
    fun `should return successful result without retrying`() = runTest {
        // Given
        var attempts = 0

        // When
        val result = retry.executeWithRetry {
            attempts++
            "Success"
        }

        // Then
        assertEquals("Success", result.getOrThrow())
    }

    @Test
    fun `should propagate cancellation immediately`() = runTest {
        // Given
        val expectedMessage = "Cancelled"

        // When
        val thrownException = runCatching {
            retry.executeWithRetry<Unit> {
                throw CancellationException(expectedMessage)
            }
        }.exceptionOrNull()

        // Then
        assertEquals(expectedMessage, thrownException?.message)
    }

    @Test
    fun `should return failure for non retryable exception`() = runTest {
        // Given
        val expectedMessage = "Validation failed"

        // When
        val result = retry.executeWithRetry<String> {
            throw IllegalArgumentException(expectedMessage)
        }

        // Then
        assertEquals(expectedMessage, result.exceptionOrNull()?.message)
    }
}

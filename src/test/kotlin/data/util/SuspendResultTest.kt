package org.example.test.data.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.example.data.util.suspendResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class SuspendResultTest {

    @Test
    fun `should return successful result when block succeeds`() = runTest {
        // Given
        val expectedValue = "Package loaded"

        // When
        val result = suspendResult {
            expectedValue
        }

        // Then
        assertEquals(expectedValue, result.getOrThrow())
    }

    @Test
    fun `should return failed result when block throws ordinary exception`() = runTest {
        // Given
        val expectedException = IllegalStateException("Loading failed")

        // When
        val result = suspendResult<Unit> {
            throw expectedException
        }

        // Then
        assertSame(expectedException, result.exceptionOrNull())
    }

    @Test
    fun `should rethrow cancellation exception`() = runTest {
        // Given
        val expectedException = CancellationException("Cancelled")

        // When
        val actualException = runCatching {
            suspendResult<Unit> {
                throw expectedException
            }
        }.exceptionOrNull()

        // Then
        assertSame(expectedException, actualException)
    }
}

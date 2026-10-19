package org.example.test.data.ratelimit

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.example.data.ratelimit.RateLimiter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RateLimiterTest {

    @Test
    fun `tryAcquire should consume token when available`() {
        val rateLimiter = RateLimiter(
            maxBucketCapacity = 1,
            refillRatePerSecond = 1
        )

        assertTrue(rateLimiter.tryAcquire())
        assertFalse(rateLimiter.tryAcquire())
    }

    @Test
    fun `tryAcquire should allow requests up to bucket capacity`() {
        val rateLimiter = RateLimiter(
            maxBucketCapacity = 3,
            refillRatePerSecond = 1
        )

        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
        assertTrue(rateLimiter.tryAcquire())
        assertFalse(rateLimiter.tryAcquire())
    }

    @Test
    fun `constructor should reject invalid bucket capacity`() {
        val result = runCatching {
            RateLimiter(
                maxBucketCapacity = 0,
                refillRatePerSecond = 1
            )
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `constructor should reject invalid refill rate`() {
        val result = runCatching {
            RateLimiter(
                maxBucketCapacity = 1,
                refillRatePerSecond = 0
            )
        }

        assertTrue(result.isFailure)
    }

    @Test
    fun `concurrent requests should not consume more tokens than capacity`() = runTest {
        val capacity = 10

        val rateLimiter = RateLimiter(
            maxBucketCapacity = capacity,
            refillRatePerSecond = 1
        )

        val results = List(100) {
            async {
                rateLimiter.tryAcquire()
            }
        }.awaitAll()

        assertEquals(
            capacity,
            results.count { it }
        )
    }
}

package org.example.data.ratelimit

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.delay

class RateLimiter(
    private val maxBucketCapacity: Int,
    private val refillRatePerSecond: Int
) {

    private val availableTokens = AtomicInteger(maxBucketCapacity)
    private val lastRefillNanos = AtomicLong(System.nanoTime())

    init {
        validateConfiguration()
    }

    fun tryAcquire(): Boolean {
        refillTokens()
        return consumeToken()
    }

    suspend fun acquire() {
        while (!tryAcquire()) {
            delay(waitTimeMillis())
        }
    }

    private fun consumeToken(): Boolean {
        while (true) {
            val currentTokens = availableTokens.get()

            if (currentTokens == EMPTY_BUCKET) {
                return false
            }

            val tokenConsumed = availableTokens.compareAndSet(
                currentTokens,
                currentTokens - TOKEN_COST
            )

            if (tokenConsumed) {
                return true
            }
        }
    }

    private fun refillTokens() {
        while (true) {
            val previousRefillTime = lastRefillNanos.get()
            val currentTime = System.nanoTime()
            val elapsedNanos = currentTime - previousRefillTime
            val tokensToAdd = calculateTokensToAdd(elapsedNanos)

            if (tokensToAdd == EMPTY_BUCKET) {
                return
            }

            val refillDuration = nanosRequiredFor(tokensToAdd)
            val newRefillTime = previousRefillTime + refillDuration

            val refillClaimed = lastRefillNanos.compareAndSet(
                previousRefillTime,
                newRefillTime
            )

            if (refillClaimed) {
                addTokens(tokensToAdd)
                return
            }
        }
    }

    private fun calculateTokensToAdd(elapsedNanos: Long): Int {
        return (elapsedNanos / nanosPerToken()).toInt()
    }

    private fun nanosRequiredFor(tokens: Int): Long {
        return tokens * nanosPerToken()
    }

    private fun nanosPerToken(): Long {
        return NANOS_PER_SECOND / refillRatePerSecond
    }

    private fun addTokens(tokensToAdd: Int) {
        availableTokens.updateAndGet { currentTokens ->
            minOf(maxBucketCapacity, currentTokens + tokensToAdd)
        }
    }

    private fun waitTimeMillis(): Long {
        return maxOf(MINIMUM_WAIT_MILLIS, MILLIS_PER_SECOND / refillRatePerSecond)
    }

    private fun validateConfiguration() {
        require(maxBucketCapacity > EMPTY_BUCKET) {
            "maxBucketCapacity must be greater than zero."
        }

        require(refillRatePerSecond > EMPTY_BUCKET) {
            "refillRatePerSecond must be greater than zero."
        }
    }

    private companion object {
        const val EMPTY_BUCKET = 0
        const val TOKEN_COST = 1
        const val NANOS_PER_SECOND = 1_000_000_000L
        const val MILLIS_PER_SECOND = 1_000L
        const val MINIMUM_WAIT_MILLIS = 1L
    }
}

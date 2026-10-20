package org.example.test.data.circuitbreaker

import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.example.data.circuitbreaker.CircuitBreaker
import org.example.data.exception.CallNotPermittedException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CircuitBreakerTest {

    private fun createCircuitBreaker(currentTimeNanos: () -> Long = { 0L }): CircuitBreaker =
        CircuitBreaker(
            failureThreshold = 5,
            resetTimeoutMillis = 5_000L,
            isNetworkFailure = { it is IOException },
            currentTimeNanos = currentTimeNanos
        )

    private suspend fun failFiveTimes(circuitBreaker: CircuitBreaker) {
        repeat(5) {
            circuitBreaker.execute {
                Result.failure<Unit>(IOException("Network unavailable"))
            }
        }
    }

    private suspend fun failFourTimes(
        circuitBreaker: CircuitBreaker
    ) {
        repeat(4) {
            circuitBreaker.execute {
                Result.failure<Unit>(IOException("Network unavailable"))
            }
        }
    }


    @Test
    fun `allows successful requests when circuit is closed`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        val result = circuitBreaker.execute {
            Result.success("Success")
        }
        assertEquals("Success", result.getOrThrow())
    }


    @Test
    fun `allows fifth request after four network failures`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        var remoteCalls = 0
        failFourTimes(circuitBreaker)
        circuitBreaker.execute {
            remoteCalls++
            Result.failure<Unit>(IOException("Network unavailable"))
        }
        assertEquals(1, remoteCalls)
    }


    @Test
    fun `rejects sixth request after five network failures`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        failFiveTimes(circuitBreaker)
        val result = runCatching {
            circuitBreaker.execute {
                Result.success(Unit)
            }
        }
        assertTrue(result.exceptionOrNull() is CallNotPermittedException)
    }

    @Test
    fun `does not execute operation while circuit is open`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        var remoteCalls = 0
        failFiveTimes(circuitBreaker)
        runCatching {
            circuitBreaker.execute {
                remoteCalls++
                Result.success(Unit)
            }
        }
        assertEquals(0, remoteCalls)
    }

    @Test
    fun `resets failure count after successful request`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        failFourTimes(circuitBreaker)
        circuitBreaker.execute {
            Result.success(Unit)
        }
        failFourTimes(circuitBreaker)
        val result = circuitBreaker.execute {
            Result.success("Allowed")
        }
        assertEquals("Allowed", result.getOrThrow())
    }


    @Test
    fun `does not count validation errors as network failures`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        repeat(5) {
            circuitBreaker.execute {
                Result.failure<Unit>(IllegalArgumentException("Invalid data"))
            }
        }
        val result = circuitBreaker.execute {
            Result.success("Allowed")
        }
        assertTrue(result.isSuccess)
    }

    @Test
    fun `resets consecutive failures after non-network error`() = runTest {
        val circuitBreaker = createCircuitBreaker()
        failFourTimes(circuitBreaker)
        circuitBreaker.execute {
            Result.failure<Unit>(IllegalArgumentException("Invalid data"))
        }
        failFourTimes(circuitBreaker)
        val result = circuitBreaker.execute {
            Result.success("Allowed")
        }
        assertTrue(result.isSuccess)
    }


    @Test
    fun `remains open before five seconds`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 4_999_999_999L
        val result = runCatching {
            circuitBreaker.execute {
                Result.success(Unit)
            }
        }
        assertTrue(result.exceptionOrNull() is CallNotPermittedException)
    }


    @Test
    fun `allows recovery probe after five seconds`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        val result = circuitBreaker.execute {
            Result.success("Recovered")
        }
        assertEquals("Recovered", result.getOrThrow())
    }


    @Test
    fun `closes circuit after successful recovery probe`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        circuitBreaker.execute {
            Result.success(Unit)
        }
        val result = circuitBreaker.execute {
            Result.success("Normal operation")
        }
        assertTrue(result.isSuccess)
    }


    @Test
    fun `resets failure counter after successful probe`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        circuitBreaker.execute {
            Result.success(Unit)
        }
        failFourTimes(circuitBreaker)
        val result = circuitBreaker.execute {
            Result.success("Still closed")
        }
        assertTrue(result.isSuccess)
    }


    @Test
    fun `reopens circuit after failed recovery probe`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        circuitBreaker.execute {
            Result.failure<Unit>(IOException("Probe failed"))
        }
        val result = runCatching {
            circuitBreaker.execute {
                Result.success(Unit)
            }
        }
        assertTrue(result.exceptionOrNull() is CallNotPermittedException)
    }


    @Test
    fun `starts new five second timeout after failed probe`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        circuitBreaker.execute {
            Result.failure<Unit>(IOException("Probe failed"))
        }
        timeNanos = 9_999_999_999L
        val result = runCatching {
            circuitBreaker.execute {
                Result.success(Unit)
            }
        }
        assertTrue(result.exceptionOrNull() is CallNotPermittedException)
    }


    @Test
    fun `allows another probe after second timeout`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        circuitBreaker.execute {
            Result.failure<Unit>(IOException("Probe failed"))
        }
        timeNanos = 10_000_000_000L
        val result = circuitBreaker.execute {
            Result.success("Recovered again")
        }
        assertEquals("Recovered again", result.getOrThrow())
    }


    @Test
    fun `rejects second request while recovery probe is running`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        val probeStarted = CompletableDeferred<Unit>()
        val finishProbe = CompletableDeferred<Unit>()
        val firstProbe = async {
            circuitBreaker.execute {
                probeStarted.complete(Unit)
                finishProbe.await()
                Result.success(Unit)
            }
        }
        probeStarted.await()
        val secondResult = runCatching {
            circuitBreaker.execute {
                Result.success(Unit)
            }
        }
        finishProbe.complete(Unit)
        firstProbe.await()
        assertTrue(secondResult.exceptionOrNull() is CallNotPermittedException)
    }


    @Test
    fun `does not execute second concurrent probe`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        val probeStarted = CompletableDeferred<Unit>()
        val finishProbe = CompletableDeferred<Unit>()
        val remoteCalls = AtomicInteger(0)
        val firstProbe = async {
            circuitBreaker.execute {
                remoteCalls.incrementAndGet()
                probeStarted.complete(Unit)
                finishProbe.await()
                Result.success(Unit)
            }
        }
        probeStarted.await()
        runCatching {
            circuitBreaker.execute {
                remoteCalls.incrementAndGet()
                Result.success(Unit)
            }
        }
        finishProbe.complete(Unit)
        firstProbe.await()
        assertEquals(1, remoteCalls.get())
    }


    @Test
    fun `reopens circuit when recovery probe is cancelled`() = runTest {
        var timeNanos = 0L
        val circuitBreaker = createCircuitBreaker {
            timeNanos
        }
        failFiveTimes(circuitBreaker)
        timeNanos = 5_000_000_000L
        val probeStarted = CompletableDeferred<Unit>()
        val finishProbe = CompletableDeferred<Unit>()
        val probe = async {
            circuitBreaker.execute {
                probeStarted.complete(Unit)
                finishProbe.await()
                Result.success(Unit)
            }
        }
        probeStarted.await()
        probe.cancel()
        probe.join()
        val result = runCatching {
            circuitBreaker.execute {
                Result.success(Unit)
            }
        }
        assertTrue(result.exceptionOrNull() is CallNotPermittedException)
    }


    @Test
    fun `rejects zero failure threshold`() {
        val result = runCatching {
            CircuitBreaker(failureThreshold = 0, isNetworkFailure = { it is IOException })
        }
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }


    @Test
    fun `rejects zero reset timeout`() {
        val result = runCatching {
            CircuitBreaker(resetTimeoutMillis = 0L, isNetworkFailure = { it is IOException })
        }
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }
}



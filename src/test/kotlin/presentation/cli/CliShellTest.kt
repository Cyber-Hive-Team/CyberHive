package org.example.test.presentation.cli

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import org.example.domain.model.PackageSectorInput
import org.example.domain.model.RegionalZone
import org.example.domain.model.VehicleTelemetry
import org.example.domain.model.result.FleetTrackingEvent
import org.example.domain.model.result.RoutingResult
import org.example.domain.usecase.ClassifyPackageSectorUseCase
import org.example.domain.usecase.FindOptimalPathUseCase
import org.example.domain.usecase.TrackActiveFleetUseCase
import org.example.presentation.cli.CliRuntime
import org.example.presentation.cli.CliShell
import org.example.presentation.cli.ConsoleInput
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CliShellTest {
    private class Fixture(scope: TestScope) {
        val commands = Channel<String>(Channel.UNLIMITED)
        val messages = mutableListOf<String>()
        val input = mockk<ConsoleInput>()
        val route = mockk<FindOptimalPathUseCase>()
        val classify = mockk<ClassifyPackageSectorUseCase>()
        val track = mockk<TrackActiveFleetUseCase>()
        val shell: CliShell

        init {
            coEvery { input.readLine() } coAnswers {
                commands.receiveCatching().getOrNull()
            }
            shell = CliShell(
                input = input,
                findOptimalPath = route,
                classifyPackageSector = classify,
                trackActiveFleet = track,
                vehicleIds = listOf("V-1"),
                runtime = CliRuntime(
                    computationDispatcher = StandardTestDispatcher(scope.testScheduler),
                    output = messages::add
                )
            )
        }

        val job = scope.backgroundScope.launch { shell.run() }
    }

    @Test
    fun `exit cancels a running calculation`() = runTest {
        // Given
        val fixture = Fixture(this)
        val stopped = CompletableDeferred<Unit>()
        coEvery { fixture.route(any(), any()) } coAnswers {
            try {
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }
        fixture.commands.send("route A B")
        runCurrent()

        // When
        fixture.commands.send("exit")
        fixture.job.join()

        // Then
        assertTrue(stopped.isCompleted)
    }

    @Test
    fun `exit stops the loading indicator`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } coAnswers { awaitCancellation() }
        fixture.commands.send("route A B")
        runCurrent()
        fixture.commands.send("exit")
        fixture.job.join()
        val dotsBefore = fixture.messages.count { it == "." }

        // When
        advanceTimeBy(1000)
        runCurrent()

        // Then
        assertEquals(dotsBefore, fixture.messages.count { it == "." })
    }

    @Test
    fun `route uses one loading indicator`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } coAnswers { awaitCancellation() }

        // When
        fixture.commands.send("route A B")
        runCurrent()
        advanceTimeBy(900)
        runCurrent()

        // Then
        assertEquals(4, fixture.messages.count { it == "." })
    }

    @Test
    fun `route displays the calculated path`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route("A", "B") } returns RoutingResult(
            path = listOf(
                mockk { every { id } returns "A" },
                mockk { every { id } returns "B" }
            ),
            distanceKm = 12.0
        )

        // When
        fixture.commands.send("route A B")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains("Path: A -> B, 12.0 km"))
    }

    @Test
    fun `route accepts repeated whitespace`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route("A", "B") } returns RoutingResult(
            path = listOf(
                mockk { every { id } returns "A" },
                mockk { every { id } returns "B" }
            ),
            distanceKm = 12.0
        )

        // When
        fixture.commands.send("  route   A\tB  ")
        runCurrent()

        // Then
        coVerify(exactly = 1) { fixture.route("A", "B") }
    }

    @Test
    fun `route completion stops the loading indicator`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } coAnswers {
            delay(700)
            RoutingResult(emptyList(), Double.POSITIVE_INFINITY)
        }
        fixture.commands.send("route A B")
        runCurrent()
        advanceTimeBy(701)
        runCurrent()
        val dotsBefore = fixture.messages.count { it == "." }

        // When
        advanceTimeBy(1000)
        runCurrent()

        // Then
        assertEquals(dotsBefore, fixture.messages.count { it == "." })
    }

    @Test
    fun `cancel stops the current calculation`() = runTest {
        // Given
        val fixture = Fixture(this)
        val stopped = CompletableDeferred<Unit>()
        coEvery { fixture.route(any(), any()) } coAnswers {
            try {
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }
        fixture.commands.send("route A B")
        runCurrent()

        // When
        fixture.commands.send("cancel")
        runCurrent()

        // Then
        assertTrue(stopped.isCompleted)
    }

    @Test
    fun `a new calculation can start after cancellation`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } coAnswers { awaitCancellation() }
        coEvery { fixture.classify(any()) } returns Result.success(RegionalZone.NORTH)
        fixture.commands.send("route A B")
        runCurrent()
        fixture.commands.send("cancel")
        runCurrent()

        // When
        fixture.commands.send("classify 31.9 35.2 12.5")
        runCurrent()

        // Then
        coVerify(exactly = 1) {
            fixture.classify(PackageSectorInput("CLI-QUERY", 31.9, 35.2, 12.5))
        }
    }

    @Test
    fun `classification displays the predicted sector`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.classify(any()) } returns Result.success(RegionalZone.NORTH)

        // When
        fixture.commands.send("classify 31.9 35.2 12.5")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains("Predicted sector: NORTH"))
    }

    @Test
    fun `end of input cancels telemetry`() = runTest {
        // Given
        val fixture = Fixture(this)
        val stopped = CompletableDeferred<Unit>()
        every { fixture.track(any()) } returns flow {
            try {
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }
        fixture.commands.send("telemetry")
        runCurrent()

        // When
        fixture.commands.close()
        fixture.job.join()

        // Then
        assertTrue(stopped.isCompleted)
    }

    @Test
    fun `exit cancels telemetry`() = runTest {
        // Given
        val fixture = Fixture(this)
        val stopped = CompletableDeferred<Unit>()
        every { fixture.track(any()) } returns flow {
            try {
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }
        fixture.commands.send("telemetry")
        runCurrent()

        // When
        fixture.commands.send("exit")
        fixture.job.join()

        // Then
        assertTrue(stopped.isCompleted)
    }

    @Test
    fun `stop cancels telemetry`() = runTest {
        // Given
        val fixture = Fixture(this)
        val stopped = CompletableDeferred<Unit>()
        every { fixture.track(any()) } returns flow {
            try {
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }
        fixture.commands.send("telemetry")
        runCurrent()

        // When
        fixture.commands.send("stop")
        runCurrent()

        // Then
        assertTrue(stopped.isCompleted)
    }

    @Test
    fun `telemetry command does not start a duplicate collector`() = runTest {
        // Given
        val fixture = Fixture(this)
        var starts = 0
        every { fixture.track(any()) } returns flow {
            starts++
            awaitCancellation()
        }
        fixture.commands.send("telemetry")
        runCurrent()

        // When
        fixture.commands.send("telemetry")
        runCurrent()

        // Then
        assertEquals(1, starts)
    }

    @Test
    fun `telemetry can restart after stop`() = runTest {
        // Given
        val fixture = Fixture(this)
        var starts = 0
        every { fixture.track(any()) } returns flow {
            starts++
            awaitCancellation()
        }
        fixture.commands.send("telemetry")
        runCurrent()
        fixture.commands.send("stop")
        runCurrent()

        // When
        fixture.commands.send("telemetry")
        runCurrent()

        // Then
        assertEquals(2, starts)
    }

    @Test
    fun `telemetry displays vehicle measurements`() = runTest {
        // Given
        val fixture = Fixture(this)
        every { fixture.track(any()) } returns flow {
            emit(FleetTrackingEvent.TelemetryUpdated(
                VehicleTelemetry("V-1", 31.0, 35.0, 50.0, 75.0, 1)
            ))
        }

        // When
        fixture.commands.send("telemetry")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains(
            "[telemetry] V-1 | Speed: 50.0 km/h | Fuel: 75.0% | GPS: 31.0, 35.0"
        ))
    }

    @Test
    fun `telemetry displays delay notifications`() = runTest {
        // Given
        val fixture = Fixture(this)
        every { fixture.track(any()) } returns flow {
            emit(FleetTrackingEvent.PotentialDelayDetected("V-1", 1))
        }

        // When
        fixture.commands.send("telemetry")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains("[delay] Potential delay detected: V-1"))
    }

    @Test
    fun `parent cancellation stops telemetry`() = runTest {
        // Given
        val fixture = Fixture(this)
        val stopped = CompletableDeferred<Unit>()
        every { fixture.track(any()) } returns flow {
            try {
                awaitCancellation()
            } finally {
                stopped.complete(Unit)
            }
        }
        fixture.commands.send("telemetry")
        runCurrent()

        // When
        fixture.job.cancel()
        fixture.job.join()

        // Then
        assertTrue(stopped.isCompleted)
    }

    @Test
    fun `classification rejects a non finite latitude`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify NaN 35 1")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects latitude outside the valid range`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 91 35 1")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects a non finite longitude`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 Infinity 1")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects longitude outside the valid range`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 181 1")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects zero weight`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 35 0")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects negative weight`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 35 -1")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects NaN weight`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 35 NaN")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects infinite weight`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 35 Infinity")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects non numeric input`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify text 35 1")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `classification rejects missing arguments`() = runTest {
        // Given
        val fixture = Fixture(this)

        // When
        fixture.commands.send("classify 31 35")
        runCurrent()

        // Then
        coVerify(exactly = 0) { fixture.classify(any()) }
    }

    @Test
    fun `route failure displays an error`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } throws IllegalStateException("route offline")

        // When
        fixture.commands.send("route A B")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains("Error: route offline"))
    }

    @Test
    fun `classification failure displays an error`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.classify(any()) } returns Result.failure(
            IllegalStateException("no training data")
        )

        // When
        fixture.commands.send("classify 31 35 1")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains("Error: no training data"))
    }

    @Test
    fun `telemetry failure displays an error`() = runTest {
        // Given
        val fixture = Fixture(this)
        every { fixture.track(any()) } returns flow {
            throw IllegalStateException("stream offline")
        }

        // When
        fixture.commands.send("telemetry")
        runCurrent()

        // Then
        assertTrue(fixture.messages.contains("Telemetry error: stream offline"))
    }

    @Test
    fun `help remains available after route failure`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } throws IllegalStateException("offline")
        fixture.commands.send("route A B")
        runCurrent()
        fixture.messages.clear()

        // When
        fixture.commands.send("help")
        runCurrent()

        // Then
        assertTrue(fixture.messages.any { it.contains("Commands:") })
    }

    @Test
    fun `help remains available after classification failure`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.classify(any()) } returns Result.failure(IllegalStateException("offline"))
        fixture.commands.send("classify 31 35 1")
        runCurrent()
        fixture.messages.clear()

        // When
        fixture.commands.send("help")
        runCurrent()

        // Then
        assertTrue(fixture.messages.any { it.contains("Commands:") })
    }

    @Test
    fun `help remains available after telemetry failure`() = runTest {
        // Given
        val fixture = Fixture(this)
        every { fixture.track(any()) } returns flow { throw IllegalStateException("offline") }
        fixture.commands.send("telemetry")
        runCurrent()
        fixture.messages.clear()

        // When
        fixture.commands.send("help")
        runCurrent()

        // Then
        assertTrue(fixture.messages.any { it.contains("Commands:") })
    }

    @Test
    fun `an active calculation prevents starting another calculation`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.route(any(), any()) } coAnswers { awaitCancellation() }
        fixture.commands.send("route A B")
        runCurrent()

        // When
        fixture.commands.send("route C D")
        runCurrent()

        // Then
        coVerify(exactly = 1) { fixture.route(any(), any()) }
    }

    @Test
    fun `cancellation returned inside Result is not displayed as an error`() = runTest {
        // Given
        val fixture = Fixture(this)
        coEvery { fixture.classify(any()) } returns Result.failure(
            CancellationException("cancelled")
        )

        // When
        fixture.commands.send("classify 31 35 1")
        runCurrent()

        // Then
        assertFalse(fixture.messages.any { it.startsWith("Error:") })
    }
}



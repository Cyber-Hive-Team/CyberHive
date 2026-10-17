package org.example.presentation.cli

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class InteractiveShellTest {
    private class TestTerminal : Terminal {
        val input = Channel<String>(Channel.UNLIMITED)
        val output = mutableListOf<String>()
        override suspend fun readLine(prompt: String): String? = input.receiveCatching().getOrNull()
        override fun printLine(message: String) { output += message }
        fun send(vararg lines: String) { lines.forEach { input.trySend(it).getOrThrow() } }
    }

    @Test
    fun `menu remains responsive and exit cancels route and telemetry`() = runTest {
        val terminal = TestTerminal()
        var routeCancelled = false
        var streamCancelled = false
        val shell = InteractiveShell(terminal, RouteCalculator { _, _ ->
            try { awaitCancellation() } finally { routeCancelled = true }
        }, PackageSectorClassifier { "NORTH" }, FleetTelemetry {
            flow {
                try { emit("vehicle update"); awaitCancellation() }
                finally { streamCancelled = true }
            }
        })
        val session = launch { shell.run() }
        terminal.send("1", "WH-001", "WH-002", "3")
        runCurrent()
        assertTrue(terminal.output.contains("vehicle update"))
        assertTrue(terminal.output.contains("Calculating route..."))
        terminal.send("0")
        session.join()
        assertTrue(routeCancelled)
        assertTrue(streamCancelled)
        assertTrue(session.children.none())
    }

    @Test
    fun `classification validates input and displays result`() = runTest {
        val terminal = TestTerminal()
        val received = mutableListOf<PackageFeatures>()
        val shell = InteractiveShell(terminal, RouteCalculator { _, _ -> "route" },
            PackageSectorClassifier { received += it; "CENTRAL" }, FleetTelemetry { flow {} })
        val session = launch { shell.run() }
        terminal.send("2", "NaN 35 5", "2", "31 35 -1", "2", "31.9 35.2 5")
        runCurrent()
        assertEquals(listOf(PackageFeatures(31.9, 35.2, 5.0)), received)
        assertTrue(terminal.output.contains("Predicted sector: CENTRAL"))
        terminal.send("exit")
        session.join()
    }

    @Test
    fun `stop awaits collector cleanup and duplicate start is ignored`() = runTest {
        val terminal = TestTerminal()
        var starts = 0
        var active = false
        val shell = InteractiveShell(terminal, RouteCalculator { _, _ -> "route" },
            PackageSectorClassifier { "NORTH" }, FleetTelemetry {
                flow<String> {
                    starts++
                    active = true
                    try { awaitCancellation() } finally { active = false }
                }
            })
        val session = launch { shell.run() }
        terminal.send("3", "3")
        runCurrent()
        assertEquals(1, starts)
        terminal.send("4")
        runCurrent()
        assertFalse(active)
        terminal.send("3")
        runCurrent()
        assertEquals(2, starts)
        terminal.send("0")
        session.join()
        assertFalse(active)
    }

    @Test
    fun `command failure stops indicator and menu still accepts commands`() = runTest {
        val terminal = TestTerminal()
        val finish = CompletableDeferred<Unit>()
        val shell = InteractiveShell(terminal, RouteCalculator { _, _ ->
            finish.await()
            error("route unavailable")
        }, PackageSectorClassifier { "NORTH" }, FleetTelemetry { flow {} })
        val session = launch { shell.run() }
        terminal.send("1", "WH-001", "WH-002")
        runCurrent()
        finish.complete(Unit)
        runCurrent()
        assertTrue(terminal.output.contains("Route failed: route unavailable"))
        terminal.send("2", "31 35 5")
        runCurrent()
        assertTrue(terminal.output.contains("Predicted sector: NORTH"))
        terminal.input.close()
        session.join()
    }
}

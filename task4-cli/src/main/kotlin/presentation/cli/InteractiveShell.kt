package org.example.presentation.cli

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class InteractiveShell(
    private val terminal: Terminal,
    private val routes: RouteCalculator,
    private val classifier: PackageSectorClassifier,
    private val telemetry: FleetTelemetry,
) {
    suspend fun run(): Unit = supervisorScope {
        val session = this
        val operations = mutableListOf<Job>()
        var stream: Job? = null
        try {
            terminal.printLine("CyberHive | type 0 or exit to quit")
            while (isActive) {
                operations.removeAll { it.isCompleted }
                when (terminal.readLine("1 Route | 2 Classify | 3 Start telemetry | 4 Stop telemetry | 0 Exit")
                    ?.trim()?.lowercase()) {
                    null, "0", "exit" -> break
                    "1" -> {
                        val start = terminal.readLine("Start warehouse ID (e.g. WH-001):") ?: break
                        val destination = terminal.readLine("Destination warehouse ID:") ?: break
                        if (!start.matches(WAREHOUSE_ID) || !destination.matches(WAREHOUSE_ID)) {
                            terminal.printLine("Use warehouse IDs in the form WH-001.")
                            continue
                        }
                        operations += session.command("Route") {
                            withLoading { terminal.printLine(routes.calculate(start, destination)) }
                        }
                    }
                    "2" -> {
                        val text = terminal.readLine("Latitude longitude weightKg (e.g. 31.9 35.2 5):") ?: break
                        val features = parseFeatures(text)
                        if (features == null) terminal.printLine("Enter valid latitude, longitude and positive weight.")
                        else operations += session.command("Classification") {
                            terminal.printLine("Predicted sector: ${classifier.classify(features)}")
                        }
                    }
                    "3" -> if (stream?.isActive == true) {
                        terminal.printLine("Telemetry is already running.")
                    } else {
                        stream = session.command("Telemetry") {
                            telemetry.updates().collect { terminal.printLine(it) }
                        }
                    }
                    "4" -> {
                        stream?.cancelAndJoin()
                        stream = null
                        terminal.printLine("Telemetry stopped.")
                    }
                    else -> terminal.printLine("Unknown command. Choose 0, 1, 2, 3 or 4.")
                }
            }
        } finally {
            // Cancels the session parent and all command/stream/loading children.
            session.cancel()
            terminal.printLine("Goodbye. Session cancelled.")
        }
    }

    private fun CoroutineScope.command(name: String, action: suspend () -> Unit): Job = launch {
        try {
            action()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            terminal.printLine("$name failed: ${exception.message ?: exception.javaClass.simpleName}")
        }
    }

    private suspend fun withLoading(action: suspend () -> Unit) = coroutineScope {
        val indicator = launch {
            while (isActive) {
                terminal.printLine("Calculating route...")
                delay(500)
            }
        }
        try {
            action()
        } finally {
            indicator.cancelAndJoin()
        }
    }

    private fun parseFeatures(text: String): PackageFeatures? {
        val values = text.trim().split(Regex("\\s+"))
        if (values.size != 3) return null
        val numbers = values.map { it.toDoubleOrNull() ?: return null }
        if (numbers.any { !it.isFinite() } || numbers[0] !in -90.0..90.0 ||
            numbers[1] !in -180.0..180.0 || numbers[2] <= 0.0) return null
        return PackageFeatures(numbers[0], numbers[1], numbers[2])
    }

    private companion object {
        val WAREHOUSE_ID = Regex("WH-\\d{3}")
    }
}

package org.example.presentation.cli

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.IOException
import org.example.domain.model.exception.DomainException
import org.example.domain.model.PackageSectorInput
import org.example.domain.model.result.FleetTrackingEvent
import org.example.domain.usecase.ClassifyPackageSectorUseCase
import org.example.domain.usecase.FindOptimalPathUseCase
import org.example.domain.usecase.TrackActiveFleetUseCase

private const val HELP_TEXT = """
Commands:
  route <from> <to>               calculate the optimal route
  classify <lat> <lon> <weight>   predict the delivery sector
  telemetry                       stream live fleet updates
  stop                            stop telemetry
  cancel                          cancel the current calculation
  help                            show this message
  exit                            quit and cancel all tasks
"""

private const val ROUTE_PARTS_COUNT = 3
private const val CLASSIFY_PARTS_COUNT = 4
private const val DOT_DELAY_MS = 300L
private const val CLI_PACKAGE_ID = "CLI-QUERY"
private const val LATITUDE_LIMIT = 90.0
private const val LONGITUDE_LIMIT = 180.0
private const val WEIGHT_INDEX = 3

class CliShell(
    private val input: ConsoleInput,
    private val findOptimalPath: FindOptimalPathUseCase,
    private val classifyPackageSector: ClassifyPackageSectorUseCase,
    private val trackActiveFleet: TrackActiveFleetUseCase,
    private val vehicleIds: List<String>,
    private val runtime: CliRuntime = CliRuntime()
) {
    private val output = runtime.output

    private class RunningJobs {
        var calculation: Job? = null
        var telemetry: Job? = null
    }

    suspend fun run(): Unit = supervisorScope {
        try {
            commandLoop(this)
        } finally {
            coroutineContext.cancelChildren()
        }
        output("Bye!")
    }

    private suspend fun commandLoop(scope: CoroutineScope) {
        val jobs = RunningJobs()
        output(HELP_TEXT)
        while (currentCoroutineContext().isActive) {
            output("> ")
            val line = input.readLine() ?: return
            val parts = line.trim().split(Regex("\\s+"))
            if (parts[0].equals("exit", ignoreCase = true)) return
            handleCommand(parts, scope, jobs)
        }
    }

    private suspend fun handleCommand(parts: List<String>, scope: CoroutineScope, jobs: RunningJobs) {
        when (parts[0].lowercase()) {
            "route", "classify" -> startCalculation(parts, scope, jobs)
            "telemetry" -> startTelemetry(scope, jobs)
            "stop" -> {
                jobs.telemetry?.cancelAndJoin()
                jobs.telemetry = null
                output("Telemetry stopped.")
            }
            "cancel" -> {
                jobs.calculation?.cancelAndJoin()
                jobs.calculation = null
                output("Calculation cancelled.")
            }
            "help" -> output(HELP_TEXT)
            "" -> Unit
            else -> output("Unknown command. Type 'help'.")
        }
    }

    private fun startCalculation(parts: List<String>, scope: CoroutineScope, jobs: RunningJobs) {
        if (jobs.calculation?.isActive == true) {
            output("A calculation is running. Type 'cancel' first.")
            return
        }
        jobs.calculation = scope.launch(runtime.computationDispatcher) {
            reportFailure("Error") {
                when (parts[0].lowercase()) {
                    "route" -> handleRoute(parts)
                    else -> handleClassify(parts)
                }
            }
        }
    }

    private fun startTelemetry(scope: CoroutineScope, jobs: RunningJobs) {
        when {
            jobs.telemetry?.isActive == true -> output("Telemetry is already running. Type 'stop' to end it.")
            vehicleIds.isEmpty() -> output("No vehicles available.")
            else -> {
                output("Telemetry started. Type 'stop' to end it.")
                jobs.telemetry = scope.launch {
                    reportFailure("Telemetry error") {
                        trackActiveFleet(vehicleIds).collect(::printEvent)
                    }
                }
            }
        }
    }

    private suspend fun handleRoute(parts: List<String>) {
        require(parts.size == ROUTE_PARTS_COUNT) { "Usage: route <from> <to>" }
        val routing = withSpinner { findOptimalPath(parts[1], parts[2]) }
        currentCoroutineContext().ensureActive()
        if (routing.path.isEmpty()) {
            output("No route found.")
        } else {
            val names = routing.path.joinToString(" -> ") { it.id }
            output("Path: $names, ${routing.distanceKm} km")
        }
    }

    private suspend fun handleClassify(parts: List<String>) {
        require(parts.size == CLASSIFY_PARTS_COUNT) { "Usage: classify <latitude> <longitude> <weight>" }
        val latitude = parts[1].toDoubleOrNull()
        val longitude = parts[2].toDoubleOrNull()
        val weight = parts[WEIGHT_INDEX].toDoubleOrNull()
        require(latitude != null && latitude.isFinite() && latitude in -LATITUDE_LIMIT..LATITUDE_LIMIT) {
            "Latitude must be between -90 and 90."
        }
        require(longitude != null && longitude.isFinite() && longitude in -LONGITUDE_LIMIT..LONGITUDE_LIMIT) {
            "Longitude must be between -180 and 180."
        }
        require(weight != null && weight.isFinite() && weight > 0.0) { "Weight must be a finite positive number." }
        val sectorInput = PackageSectorInput(CLI_PACKAGE_ID, latitude, longitude, weight)
        val result = withSpinner { classifyPackageSector(sectorInput) }
        currentCoroutineContext().ensureActive()
        output("Predicted sector: ${result.getOrThrow()}")
    }

    private suspend fun <T> withSpinner(block: suspend () -> T): T = coroutineScope {
        val spinner = launch {
            while (isActive) {
                output(".")
                delay(DOT_DELAY_MS)
            }
        }
        try {
            block()
        } finally {
            spinner.cancel()
        }
    }

    private suspend fun reportFailure(label: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: DomainException) {
            printFailure(label, failure)
        } catch (failure: IOException) {
            printFailure(label, failure)
        } catch (failure: IllegalArgumentException) {
            printFailure(label, failure)
        } catch (failure: IllegalStateException) {
            printFailure(label, failure)
        }
    }

    private suspend fun printFailure(label: String, failure: Exception) {
        currentCoroutineContext().ensureActive()
        output("$label: ${failure.message ?: failure::class.simpleName}")
    }

    private fun printEvent(event: FleetTrackingEvent) {
        when (event) {
            is FleetTrackingEvent.TelemetryUpdated -> event.telemetry.run {
                output(
                    "[telemetry] $vehicleId | Speed: $speedKmh km/h | " +
                        "Fuel: $fuelPercentage% | GPS: $latitude, $longitude"
                )
            }
            is FleetTrackingEvent.PotentialDelayDetected -> {
                output("[delay] Potential delay detected: ${event.vehicleId}")
            }
        }
    }
}

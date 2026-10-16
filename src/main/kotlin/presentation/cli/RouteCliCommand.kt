package org.example.presentation.cli

import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.domain.model.result.RoutingResult

private const val LOADING_INTERVAL_MILLIS = 500L
private val WAREHOUSE_ID_PATTERN = Regex("^WH-\\d{3}$")

class RouteCliCommand(
    private val input: ConsoleInput,
    private val computationDispatcher: CoroutineDispatcher,
    private val calculateRoute: suspend (String, String) -> RoutingResult,
) {
    suspend fun run() {
        val startId = readWarehouseId(
            "Enter start warehouse ID:"
        ) ?: return

        val destinationId = readWarehouseId(
            "Enter destination warehouse ID:"
        ) ?: return

        execute(startId, destinationId)
    }

    private suspend fun readWarehouseId(prompt: String): String? {
        while (true) {
            println(prompt)
            println("Enter 0 to return to the menu.")

            val value = input.readLine()
                ?.trim()
                ?.uppercase(Locale.ROOT)

            if (value == null || value == "0") {
                return null
            }

            if (WAREHOUSE_ID_PATTERN.matches(value)) {
                return value
            }

            println("Invalid format. Example: WH-001")
        }
    }

    // Operational errors are shown at the CLI boundary.
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private suspend fun execute(
        startId: String,
        destinationId: String,
    ) {
        try {
            val result = calculateWithLoading(
                startId,
                destinationId,
            )

            printResult(result)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            println(
                "Route failed: " +
                        (exception.message ?: "Unable to calculate route.")
            )
        }
    }

    private suspend fun calculateWithLoading(
        startId: String,
        destinationId: String,
    ): RoutingResult = coroutineScope {
        println("Calculating route...")

        val indicator = launch {
            while (isActive) {
                delay(LOADING_INTERVAL_MILLIS)
                println("Still calculating...")
            }
        }

        try {
            withContext(computationDispatcher) {
                calculateRoute(startId, destinationId)
            }
        } finally {
            indicator.cancel()
        }
    }

    private fun printResult(result: RoutingResult) {
        if (
            result.path.isEmpty() ||
            !result.distanceKm.isFinite()
        ) {
            println("No route found.")
        } else {
            val path = result.path.joinToString(" -> ") {
                it.id
            }

            println("Route: $path")
            println("Distance: ${result.distanceKm} km")
        }
    }
}

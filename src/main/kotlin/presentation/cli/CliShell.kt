package org.example.presentation.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.domain.usecase.FindOptimalPathUseCase
import org.example.domain.model.PackageSectorInput
import org.example.domain.usecase.ClassifyPackageSectorUseCase

private const val HELP_TEXT = """
Commands:
  route <from> <to>   calculate the optimal route
  classify <lat> <lon> <weight>   predict the delivery sector
  help      show this message
  exit      quit
"""

private const val ROUTE_PARTS_COUNT = 3
private const val DOT_DELAY_MS = 300L
private const val CLASSIFY_PARTS_COUNT = 4
private const val CLASSIFY_NUMBERS_COUNT = 3
private const val CLI_PACKAGE_ID = "CLI-QUERY"

class CliShell(
    private val input: ConsoleInput,
    private val findOptimalPath: FindOptimalPathUseCase,
    private val classifyPackageSector: ClassifyPackageSectorUseCase,
    private val scope: CoroutineScope
) {

    suspend fun run() {
        scope.launch { commandLoop() }.join()
        println("Bye!")
    }

    private suspend fun commandLoop() {
        println(HELP_TEXT)
        while (scope.isActive) {
            print("> ")
            val line = input.readLine() ?: return
            val parts = line.trim().split(" ")

            when (parts[0]) {
                "route" -> handleRoute(parts)
                "classify" -> handleClassify(parts)
                "help" -> println(HELP_TEXT)
                "exit" -> {
                    scope.cancel()
                    return
                }
                else -> println("Unknown command. Type 'help'.")
            }
        }
    }
    private suspend fun handleRoute(parts: List<String>) {
        if (parts.size != ROUTE_PARTS_COUNT) {
            println("Usage: route <from> <to>")
            return
        }

        val spinner = scope.launch {
            while (true) {
                print(".")
                delay(DOT_DELAY_MS)
            }
        }

        val result = runCatching { withSpinner { findOptimalPath(parts[1], parts[2]) } }
        spinner.cancel()
        println()

        result
            .onSuccess { routing ->
                val names = routing.path.joinToString(" -> ") { warehouse -> warehouse.id }
                println("Path: $names, ${routing.distanceKm} km")
            }
            .onFailure { println("Error: ${it.message}") }
    }
    private suspend fun <T> withSpinner(block: suspend () -> T): T {
        val spinner = scope.launch {
            while (true) {
                print(".")
                delay(DOT_DELAY_MS)
            }
        }
        try {
            return block()
        } finally {
            spinner.cancel()
            println()
        }
    }
    private suspend fun handleClassify(parts: List<String>) {
        val numbers = parts.drop(1).mapNotNull { it.toDoubleOrNull() }
        if (parts.size != CLASSIFY_PARTS_COUNT || numbers.size != CLASSIFY_NUMBERS_COUNT) {
            println("Usage: classify <latitude> <longitude> <weight>")
            return
        }

        val (latitude, longitude, weight) = numbers
        val sectorInput = PackageSectorInput(CLI_PACKAGE_ID, latitude, longitude, weight)

        withSpinner { classifyPackageSector(sectorInput) }
            .onSuccess { zone -> println("Predicted sector: $zone") }
            .onFailure { println("Error: ${it.message}") }
    }
}

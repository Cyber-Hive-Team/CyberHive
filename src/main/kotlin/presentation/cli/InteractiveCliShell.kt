package org.example.presentation.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.domain.usecase.FindOptimalPathUseCase

private const val ROUTE_PARTS_COUNT = 3
private const val DOT_DELAY_MS = 300L

class CliShell(
    private val input: ConsoleInput,
    private val findOptimalPath: FindOptimalPathUseCase,
    private val scope: CoroutineScope
) {

    suspend fun run() {
        scope.launch { commandLoop() }.join()
        println("Bye!")
    }

    private suspend fun commandLoop() {
        println("Commands: route <from> <to> | classify | telemetry | exit")
        while (scope.isActive) {
            print("> ")
            val line = input.readLine() ?: return
            val parts = line.trim().split(" ")

            when (parts[0]) {
                "route" -> handleRoute(parts)
                "classify" -> println("Classification is not connected yet.")
                "telemetry" -> println("Telemetry is not connected yet.")
                "exit" -> {
                    scope.cancel()
                    return
                }
                else -> println("Unknown command.")
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

        val result = runCatching { findOptimalPath(parts[1], parts[2]) }
        spinner.cancel()
        println()

        result
            .onSuccess { println("Path: ${it.path.joinToString(" -> ") { w -> w.id }}, ${it.distanceKm} km") }
            .onFailure { println("Error: ${it.message}") }
    }
}

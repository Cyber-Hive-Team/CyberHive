package org.example.presentation.cli

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.example.domain.usecase.FindOptimalPathUseCase

private const val HELP_TEXT = """
Commands:
  help      show this message
  exit      quit
"""

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
        println(HELP_TEXT)
        while (scope.isActive) {
            print("> ")
            val line = input.readLine() ?: return
            val parts = line.trim().split(" ")

            when (parts[0]) {
                "help" -> println(HELP_TEXT)
                "exit" -> {
                    scope.cancel()
                    return
                }
                else -> println("Unknown command. Type 'help'.")
            }
        }
    }
}
fun main() = kotlinx.coroutines.runBlocking {
    val scope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
    CliShell(ConsoleInput(), scope).run()
}

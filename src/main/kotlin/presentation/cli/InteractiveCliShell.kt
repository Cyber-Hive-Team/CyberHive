package org.example.presentation.cli

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive

class InteractiveCliShell(
    private val input: ConsoleInput,
) {
    suspend fun run(): Unit = coroutineScope {
        println("Welcome to Cyber Hive!")

        var running = true

        while (running && isActive) {
            printMenu()

            val command = input.readLine()?.trim() ?: "0"

            when (command) {
                "h", "help" -> {
                    println("Enter 0 to exit the application.")
                }

                "0" -> {
                    running = false
                }

                else -> {
                    println("Unknown command. Try again.")
                }
            }
        }

        println("Goodbye.")
    }

    private fun printMenu() {
        println()
        println("=== Cyber Hive Menu ===")
        println("h. Help")
        println("0. Exit")
        println("Choose a command:")
    }
}

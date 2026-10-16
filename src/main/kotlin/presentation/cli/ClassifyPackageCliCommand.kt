package org.example.presentation.cli

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ClassifyPackageCliCommand(
    private val input: ConsoleInput,
    private val computationDispatcher: CoroutineDispatcher,
    private val classifyPackage: suspend (Double, Double, Double) -> String,
) {
    suspend fun run() {
        val latitude = readNumber(
            "Latitude [-90..90], or back:",
        ) { it in -90.0..90.0 } ?: return

        val longitude = readNumber(
            "Longitude [-180..180], or back:",
        ) { it in -180.0..180.0 } ?: return

        val weight = readNumber(
            "Weight in kg (> 0), or back:",
        ) { it > 0.0 } ?: return

        println("Classifying package...")

        val sector = withContext(computationDispatcher) {
            classifyPackage(latitude, longitude, weight)
        }

        println("Predicted delivery sector: $sector")
    }

    private suspend fun readNumber(
        prompt: String,
        isValid: (Double) -> Boolean,
    ): Double? {
        while (true) {
            println(prompt)

            val text = input.readLine()?.trim() ?: return null

            if (text.equals("back", ignoreCase = true)) {
                return null
            }

            val number = text.toDoubleOrNull()

            if (number != null && number.isFinite() && isValid(number)) {
                return number
            }

            println("Invalid value. Please try again.")
        }
    }
}

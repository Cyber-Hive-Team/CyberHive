package org.example.presentation.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible

class ConsoleInput {

    suspend fun readLine(): String? = runInterruptible(Dispatchers.IO) {
        readlnOrNull()
    }
}

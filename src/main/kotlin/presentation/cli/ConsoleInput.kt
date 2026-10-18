package org.example.presentation.cli

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ConsoleInput {

    suspend fun readLine(): String? = withContext(Dispatchers.IO) {
        readlnOrNull()
    }
}

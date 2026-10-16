package org.example.presentation.cli

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class ConsoleInput(
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun readLine(): String? =
        withContext(ioDispatcher) {
            readlnOrNull()
        }
}

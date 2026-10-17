package org.example.presentation.cli

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class CliRuntime(
    val computationDispatcher: CoroutineDispatcher = Dispatchers.Default,
    val output: (String) -> Unit = { println(it) }
)

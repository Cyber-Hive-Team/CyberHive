package org.example.data.util

import kotlinx.coroutines.CancellationException

suspend fun <T> suspendResult(
    block: suspend () -> T
): Result<T> {
    return runCatching {
        block()
    }.onFailure { exception ->
        if (exception is CancellationException) {
            throw exception
        }
    }
}

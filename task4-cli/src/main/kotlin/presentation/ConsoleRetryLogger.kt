package org.example.presentation

import org.example.data.retry.RetryLogger

class ConsoleRetryLogger: RetryLogger {

    override fun onAttemptFailed(attempt: Int, error: Throwable, nextDelayMs: Long) {
        println(
            "Attempt $attempt failed: ${error::class.simpleName}. " +
                    "Retrying in ${nextDelayMs}ms"
        )
    }

    override fun onSucceeded(result: Any?) {
        println("Succeeded with result: $result")
    }

    override fun onGaveUp(error: Throwable) {
        println("Gave up: ${error::class.simpleName} - ${error.message}")
    }
}

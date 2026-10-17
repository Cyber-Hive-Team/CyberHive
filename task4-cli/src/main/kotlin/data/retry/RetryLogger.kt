package org.example.data.retry

interface RetryLogger {

        fun onAttemptFailed(attempt: Int, error: Throwable, nextDelayMs: Long) {}
        fun onSucceeded(result: Any?) {}
        fun onGaveUp(error: Throwable) {}
    }


    object NoOpRetryLogger : RetryLogger



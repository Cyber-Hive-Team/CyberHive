package org.example.presentation.cli

import java.io.ByteArrayOutputStream
import java.io.InputStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

interface Terminal {
    suspend fun readLine(prompt: String): String?
    fun printLine(message: String)
}

/** Poll buffered console bytes so waiting for input never occupies a coroutine worker. */
class ConsoleTerminal(private val input: InputStream = System.`in`) : Terminal {
    override fun printLine(message: String) = println(message)

    override suspend fun readLine(prompt: String): String? {
        printLine(prompt)
        val bytes = ByteArrayOutputStream()
        while (true) {
            currentCoroutineContext().ensureActive()
            if (input.available() == 0) {
                delay(25)
                continue
            }
            when (val byte = input.read()) {
                -1, 4, 26 -> return null
                10 -> return bytes.toString(Charsets.UTF_8.name()).trimEnd('\r')
                else -> bytes.write(byte)
            }
        }
    }
}

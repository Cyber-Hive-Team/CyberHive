package org.example.domain.command

import java.util.ArrayDeque

class CommandInvoker {

    private val history = ArrayDeque<Command>()

    fun executeCommand(command: Command): Boolean {
        val success = command.execute()
        if (success) {
            history.push(command)
        }
        return success
    }

    fun undo(): Boolean {
        if (history.isEmpty()) return false

        val lastCommand = history.pop()
        val undone = lastCommand.undo()

        if (!undone) {
            history.push(lastCommand)
        }
        return undone
    }

}

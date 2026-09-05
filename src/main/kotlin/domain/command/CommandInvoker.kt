package org.example.domain.command

import java.util.ArrayDeque
import org.example.domain.model.exception.CommandExecutionException

class CommandInvoker {

    private val history = ArrayDeque<Command>()

    fun executeCommand(command: Command): Boolean {
        val success = command.execute()
        if (success) {
            history.push(command)
        } else {
            throw CommandExecutionException("Failed to execute command: ${command::class.simpleName}")
        }
        return success
    }

    fun undo(): Boolean {
        if (history.isEmpty()) {
            throw CommandExecutionException("Cannot perform undo: Command history is empty.")
        }

        val lastCommand = history.pop()
        val undone = lastCommand.undo()

        if (!undone) {
            history.push(lastCommand)
            throw CommandExecutionException("Failed to undo command: ${lastCommand::class.simpleName}")
        }
        return undone
    }

}

package org.example.domain.command

import java.util.Stack

class CommandInvoker {

    private val undoStack = Stack<Command>()
    private val redoStack = Stack<Command>()

    val undoHistorySize: Int
        get() = undoStack.size

    val redoHistorySize: Int
        get() = redoStack.size

    fun executeCommand(command: Command): Boolean {
        val success = command.execute()
        if (success) {
            undoStack.push(command)
            redoStack.clear()
            clearRedoHistory()
            println("EXECUTE -> ${command.describe()}")
    } else {
        println("EXECUTE FAILED -> ${command.describe()}")
    }
        return success
    }

    fun undo(steps: Int = 1): Boolean {

        repeat(steps) {
        if (undoStack.isEmpty()) {
            println("UNDO: nothing left to undo")
            return false
        }
        val lastCommand = undoStack.pop()
        val undone = lastCommand.undo()

        if (!undone) {
            undoStack.push(lastCommand)
            println("UNDO FAILED -> ${lastCommand.describe()}")
            return false
        }
            redoStack.push(lastCommand)

            println("UNDO SUCCESS -> ${lastCommand.describe()}")
        }
        return true
    }

    fun redo(steps: Int = 1): Boolean {
        repeat(steps) {
            if (redoStack.isEmpty()) {
                println("REDO: nothing left to redo")
                return false
            }

            val command = redoStack.removeLast()
            val redone = command.execute()

            if (!redone) {
                redoStack.addLast(command)
                println("REDO FAILED -> ${command.describe()}")
                return false
            }

            undoStack.addLast(command)
            println("REDO SUCCESS -> ${command.describe()}")
        }

        return true
    }

    private fun clearRedoHistory() {
        if (redoStack.isEmpty()) return

        println(
            "HISTORY CLEARED: discarded ${redoStack.size} redo entr" +
                    if (redoStack.size == 1) "y" else "ies"
        )
        redoStack.clear()
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()

    fun canRedo(): Boolean = redoStack.isNotEmpty()

}

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
            clearRedoHistory()
            println("EXECUTE -> ${command.describe()}")
        } else {
        println("EXECUTE FAILED -> ${command.describe()}")
        }
        return success
    }

    fun undo(steps: Int = 1): Boolean {
            var stepsDone = 0
            var stopped = false
        while (stepsDone < steps && !stopped) {
            if (undoStack.isEmpty()) {
                println("UNDO: nothing left to undo")
                stopped = true
                continue
            }
            val lastCommand = undoStack.pop()
            val undone = lastCommand.undo()

            if (undone) {
                redoStack.push(lastCommand)
                stepsDone++
                println("UNDO SUCCESS -> ${lastCommand.describe()}")
            }else{
                undoStack.push(lastCommand)
                println("UNDO FAILED -> ${lastCommand.describe()}")
                stopped = true
            }
        }
      return stepsDone == steps
    }

    fun redo(steps: Int = 1): Boolean {
        var stepsDone = 0
        var stopped = false

        while (stepsDone < steps && !stopped) {
            if (redoStack.isEmpty()) {
                println("REDO: nothing left to redo")
                stopped = true
                continue
            }

            val command = redoStack.pop()
            val redone = command.execute()

            if (redone) {
                undoStack.push(command)
                stepsDone++
                println("REDO SUCCESS -> ${command.describe()}")
            } else {
                redoStack.push(command)
                println("REDO FAILED -> ${command.describe()}")
                stopped = true
            }
        }

        return stepsDone == steps
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

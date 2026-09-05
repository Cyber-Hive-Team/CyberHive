package org.example.domain.command

interface Command {
    fun execute(): Boolean
    fun undo(): Boolean
    fun describe(): String
}

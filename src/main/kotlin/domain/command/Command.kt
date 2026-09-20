package org.example.domain.command

interface Command {
    suspend fun execute(): Boolean
    suspend fun undo(): Boolean
    suspend fun describe(): String
}

package org.example.data.sync

interface DataSynchronizer {
    val order: Int
    suspend fun sync()
}

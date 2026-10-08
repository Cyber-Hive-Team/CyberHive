package org.example.data.sync

class SyncCoordinator(
    private val synchronizers: List<DataSynchronizer>
) {

    suspend fun syncAll() {
        synchronizers
            .sortedBy { it.order }
            .forEach { synchronizer ->
                synchronizer.sync()
            }
    }
}

package org.example.data.sync

import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.remote.VehicleRemoteDataSource
import org.example.data.mapper.sync.toRaw

class VehicleSynchronizer(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val localDataSource: VehicleDataSource
) : DataSynchronizer {
    companion object {
        private const val SYNC_ORDER = 3
    }
    override val order: Int = SYNC_ORDER

    override suspend fun sync() {
        val vehicles = remoteDataSource
            .getAll()
            .map { it.toRaw() }

        localDataSource.replaceAll(vehicles)
    }
}

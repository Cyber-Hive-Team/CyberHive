package org.example.data.sync

import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.remote.WarehouseRemoteDataSource
import org.example.data.mapper.sync.toRaw

class WarehouseSynchronizer(
    private val remoteDataSource: WarehouseRemoteDataSource,
    private val localDataSource: WarehouseDataSource
) : DataSynchronizer {
    companion object {
        private const val SYNC_ORDER = 1
    }
    override val order: Int = SYNC_ORDER

    override suspend fun sync() {
        val warehouses = remoteDataSource
            .getAll()
            .map { it.toRaw() }

        localDataSource.replaceAll(warehouses)
    }
}

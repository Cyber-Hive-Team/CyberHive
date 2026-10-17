package org.example.data.sync

import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.remote.RouteRemoteDataSource
import org.example.data.mapper.sync.toRaw

class RouteSynchronizer(
    private val remoteDataSource: RouteRemoteDataSource,
    private val localDataSource: RouteDataSource
) : DataSynchronizer {
        companion object {
            private const val SYNC_ORDER = 2
        }
    override val order: Int = SYNC_ORDER

    override suspend fun sync() {
        val routes = remoteDataSource
            .getAll()
            .map { it.toRaw() }

        localDataSource.replaceAll(routes)
    }
}

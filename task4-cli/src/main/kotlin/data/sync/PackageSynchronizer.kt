package org.example.data.sync

import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.remote.PackageRemoteDataSource
import org.example.data.mapper.sync.toRaw

class PackageSynchronizer(
    private val remoteDataSource: PackageRemoteDataSource,
    private val localDataSource: PackageDataSource
) : DataSynchronizer {
    companion object {
        private const val SYNC_ORDER = 4
    }
    override val order: Int = SYNC_ORDER

    override suspend fun sync() {
        val packages = remoteDataSource
            .getAll()
            .map { it.toRaw() }

        localDataSource.replaceAll(packages)
    }
}

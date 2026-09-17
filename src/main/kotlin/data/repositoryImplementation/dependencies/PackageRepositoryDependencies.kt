package org.example.data.repositoryImplementation.dependencies

import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.remote.PackageRemoteDatasource
import org.example.data.mapper.csv.PackageMapper
import org.example.data.mapper.remote.PackageRemoteMapper
import org.example.data.validation.PackageValidator
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository

data class PackageRepositoryDependencies(
    val localDataSource: PackageDataSource,
    val localMapper: PackageMapper,
    val validator: PackageValidator,
    val warehouseMap: Map<String, Warehouse>,
    val remoteDataSource: PackageRemoteDatasource,
    val remoteMapper: PackageRemoteMapper,
    val warehouseRepository: WarehouseRepository
)

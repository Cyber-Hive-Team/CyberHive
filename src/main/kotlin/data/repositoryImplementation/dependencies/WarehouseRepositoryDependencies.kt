package org.example.data.repositoryImplementation.dependencies

import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.remote.WarehouseRemoteDatasource
import org.example.data.mapper.csv.WarehouseMapper
import org.example.data.mapper.remote.WarehouseRemoteMapper
import org.example.data.validation.WarehouseValidator

data class WarehouseRepositoryDependencies(
    val localDataSource: WarehouseDataSource,
    val localMapper: WarehouseMapper,
    val validator: WarehouseValidator,
    val remoteDataSource: WarehouseRemoteDatasource,
    val remoteMapper: WarehouseRemoteMapper
)

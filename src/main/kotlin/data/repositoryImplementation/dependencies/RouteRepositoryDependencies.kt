package org.example.data.repositoryImplementation.dependencies

import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.remote.RouteRemoteDatasource
import org.example.data.mapper.csv.RouteMapper
import org.example.data.mapper.remote.RouteDtoMapper
import org.example.data.validation.RouteValidator
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository

data class RouteRepositoryDependencies(
    val localDataSource: RouteDataSource,
    val remoteDataSource: RouteRemoteDatasource,
    val localMapper: RouteMapper,
    val remoteMapper: RouteDtoMapper,
    val validator: RouteValidator,
    val warehouseMap: Map<String, Warehouse>,
    val warehouseRepository: WarehouseRepository
)

package org.example.data.repositoryImplementation.dependencies

import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.remote.VehicleRemoteDatasource
import org.example.data.mapper.csv.VehicleMapper
import org.example.data.mapper.remote.VehicleDtoMapper
import org.example.data.validation.VehicleValidator
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository
import org.example.data.validation.VehicleRemoteValidator

data class VehicleRepositoryDependencies(
    val localDataSource: VehicleDataSource,
    val remoteDataSource: VehicleRemoteDatasource,
    val localMapper: VehicleMapper,
    val remoteMapper: VehicleDtoMapper,
    val warehouseMap: Map<String, Warehouse>
)

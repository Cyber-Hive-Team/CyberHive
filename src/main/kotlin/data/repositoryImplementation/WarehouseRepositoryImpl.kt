package org.example.data.repositoryImplementation

import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.csv.CsvWarehouseStatusDataSource
import org.example.data.datasource.remote.WarehouseRemoteDatasource
import kotlin.random.Random
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.domain.model.Package
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository
import org.example.domain.model.WarehouseStatus
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toUpdateRequest

class WarehouseRepositoryImpl(
    private val localDataSource: WarehouseDataSource,
    private val remoteDataSource: WarehouseRemoteDatasource,
    private val statusDataSource: CsvWarehouseStatusDataSource
) : BaseRepository(), WarehouseRepository {

    private val dataExceptionMapper = DataExceptionMapper()

    private val warehouses =
        mutableListOf<Warehouse>()


    private var isLoaded = false


    override suspend fun getAllWarehouses(): Result<List<Warehouse>> {

        return runCatching {
            if (isLoaded) {
                return@runCatching warehouses.toList()
            }
            val loadedWarehouses =
                remoteDataSource
                    .getAll()
                    .mapNotNull {
                        mapWarehouseSafely(it)
                    }
            warehouses.addAll(
                loadedWarehouses
            )
            isLoaded = true
            warehouses.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }


    private fun mapWarehouseSafely(
        dto: WarehouseResponseDto
    ): Warehouse? {
        return mapSafely(dto.id) {
            dto.toDomainModel()
        }
    }


    override suspend fun addPackageToCargoQueue(
        warehouseId: String,
        cargoPackage: Package
    ): Result<Boolean> {
        return getById(warehouseId)
            .mapCatching { warehouse ->
                warehouse.addPackages(
                    listOf(cargoPackage)
                )

                true
            }.mapFailureToDomain(dataExceptionMapper)

    }


    override suspend fun sortCargoQueue(
        warehouseId: String
    ): Result<Boolean> {

        return getById(warehouseId)
            .mapCatching { warehouse ->
                warehouse.sortCargoQueue()
                true
            }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun isPackageInCargoQueue(
        warehouseId: String,
        packageId: String
    ): Result<Boolean> {

        return getById(warehouseId)
            .mapCatching { warehouse ->
                warehouse
                    .getCargoQueue()
                    .any { cargoPackage ->
                        cargoPackage.id == packageId
                    }
            }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun getAllWarehouseServices():
            Result<List<WarehouseServices>> {
        return getAllWarehouses()
            .mapCatching { warehouses ->
                warehouses.map { warehouse ->
                    WarehouseServices(
                        warehouseId = warehouse.id,
                        supportsFragileHandling =
                            Random.nextBoolean(),
                        supportsColdStorage =
                            Random.nextBoolean(),
                        supportsSpecialHandling =
                            Random.nextBoolean()
                    )
                }
            }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun getById(
        id: String
    ): Result<Warehouse> {
        val cached =
            warehouses.firstOrNull {
                it.id == id
            }
        if (cached != null) {
            return Result.success(cached)
        }
        return runCatching {
        val remoteDto =
            remoteDataSource
                .getById(id)
                ?: throw WarehouseNotFoundException()
            mapWarehouseSafely(remoteDto)
                ?: throw NullRequiredFieldException("Warehouse '$id' mapping failed.")
        }.onSuccess { warehouse ->
            warehouses.add(warehouse)
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun save(
        warehouse: Warehouse
    ): Result<Warehouse> {
        return runCatching {
            val requestDto = warehouse.toCreateRequest()
            val responseDto =
                remoteDataSource
                    .save(requestDto)
            val savedWarehouse =
                mapWarehouseSafely(responseDto)
                    ?: throw NullRequiredFieldException(
                        "Warehouse '${responseDto.id}' mapping failed."
                    )
            warehouses.add(savedWarehouse)
            savedWarehouse
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        input: UpdateWarehouseInput
    ): Result<Warehouse> {
        return runCatching {
            val requestDto = input.toUpdateRequest()
            val responseDto =
                remoteDataSource
                    .update(id = input.id, request = requestDto)
            val updatedWarehouse =
                mapWarehouseSafely(responseDto)
                    ?: throw NullRequiredFieldException(
                        "Warehouse '${input.id}' update failed."
                    )
            warehouses.removeIf {
                it.id == input.id
            }
            warehouses.add(updatedWarehouse)
            updatedWarehouse
        }.mapFailureToDomain(dataExceptionMapper)
    }
    override suspend fun delete(
        id: String
    ): Result<String> {
        return runCatching {
            val deletedId = remoteDataSource.delete(id)
            warehouses.removeIf {
                it.id == deletedId
            }
            deletedId
        }.mapFailureToDomain(dataExceptionMapper)
    }

    override suspend fun getStatus(
        warehouseId: String
    ): Result<WarehouseStatus> {

        return runCatching {
            statusDataSource
                .getStatus(warehouseId)
        }.mapFailureToDomain(dataExceptionMapper)
    }

    override suspend fun updateStatus(
        warehouseId: String,
        status: WarehouseStatus
    ): Result<Boolean> {
        return runCatching {
            statusDataSource.updateStatus(warehouseId, status)
        }.mapFailureToDomain(dataExceptionMapper)
    }
}

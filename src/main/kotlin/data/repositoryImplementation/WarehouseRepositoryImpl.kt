package org.example.data.repositoryImplementation

import kotlin.random.Random
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.data.repositoryImplementation.dependencies.WarehouseRepositoryDependencies
import org.example.domain.model.Package
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository

class WarehouseRepositoryImpl(
    private val dependencies: WarehouseRepositoryDependencies,
    private val dataExceptionMapper: DataExceptionMapper = DataExceptionMapper()
) : BaseRepository(), WarehouseRepository {


    private val warehouses =
        mutableListOf<Warehouse>()


    private var isLoaded = false


    override suspend fun getAllWarehouses(): Result<List<Warehouse>> {

        return runCatching {
            if (isLoaded) {
                return@runCatching warehouses.toList()
            }
            val loadedWarehouses =
                dependencies.remoteDataSource
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

            dependencies.remoteMapper
                .mapToDomainModel(dto)

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
            dependencies.remoteDataSource
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
            val requestDto =
                dependencies.remoteMapper
                    .mapToCreateRequest(
                        warehouse
                    )
            val responseDto =
                dependencies.remoteDataSource
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
            val requestDto =
                dependencies.remoteMapper
                    .mapToUpdateRequest(
                        name = input.name,
                        regionalZone = input.regionalZone,
                        latitude = input.latitude,
                        longitude = input.longitude
                    )
            val responseDto =
                dependencies.remoteDataSource
                    .update(id = input.id, request = requestDto)
            val updatedWarehouse =
                mapWarehouseSafely(responseDto)
                    ?: throw NullRequiredFieldException(
                        "Warehouse '$input.id' update failed."
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
            val deletedId = dependencies.remoteDataSource.delete(id)
            warehouses.removeIf {
                it.id == deletedId
            }
            deletedId
        }.mapFailureToDomain(dataExceptionMapper)
    }
}

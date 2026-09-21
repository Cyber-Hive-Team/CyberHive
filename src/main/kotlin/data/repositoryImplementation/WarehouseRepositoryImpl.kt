package org.example.data.repositoryImplementation

import kotlin.random.Random
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.repositoryImplementation.dependencies.WarehouseRepositoryDependencies
import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository


class WarehouseRepositoryImpl(
    private val dependencies: WarehouseRepositoryDependencies,
    private val dataExceptionMapper: DataExceptionMapper = DataExceptionMapper()
) : WarehouseRepository {


    private val warnings =
        mutableListOf<String>()


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
        dto: org.example.data.remote.dto.response.WarehouseResponseDto
    ): Warehouse? {

        return runCatching {
            dependencies.remoteValidator
                .validate(dto)
            dependencies.remoteMapper
                .mapToDomainModel(dto)
        }.getOrElse { exception ->
            warnings.add(
                "Warehouse '${dto.id}': ${exception.message}"
            )
            null
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
                    ?: warehouse
            warehouses.add(savedWarehouse)
            savedWarehouse
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        id: String,
        name: String?,
        regionalZone: RegionalZone?,
        latitude: Double?,
        longitude: Double?
    ): Result<Warehouse> {
        return runCatching {
            val requestDto =
                dependencies.remoteMapper
                    .mapToUpdateRequest(
                        name = name,
                        regionalZone = regionalZone,
                        latitude = latitude,
                        longitude = longitude
                    )
            val responseDto =
                dependencies.remoteDataSource
                    .update(id = id, request = requestDto)
            val updatedWarehouse =
                mapWarehouseSafely(responseDto)
                    ?: throw NullRequiredFieldException(
                        "Warehouse '$id' update failed."
                    )
            warehouses.removeIf {
                it.id == id
            }
            warehouses.add(updatedWarehouse)
            updatedWarehouse
        }.mapFailureToDomain(dataExceptionMapper)
    }
    override suspend fun delete(
        id: String
    ): Result<Boolean> {
        return runCatching {
            val deleted =
                dependencies.remoteDataSource
                    .delete(id)
            if (deleted) {
                warehouses.removeIf {
                    it.id == id
                }
            }
            deleted
        }.mapFailureToDomain(dataExceptionMapper)
    }
}

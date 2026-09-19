package org.example.data.repositoryImplementation

import org.example.data.repositoryImplementation.dependencies.WarehouseRepositoryDependencies
import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.repository.WarehouseRepository
import kotlin.random.Random


class WarehouseRepositoryImpl(
    private val dependencies: WarehouseRepositoryDependencies
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
        }
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
    ): Boolean {

        val warehouse =
            getById(warehouseId)
                ?: return false


        warehouse.addPackages(
            listOf(cargoPackage)
        )


        return true
    }


    override suspend fun sortCargoQueue(
        warehouseId: String
    ): Boolean {

        val warehouse =
            getById(warehouseId)
                ?: return false


        warehouse.sortCargoQueue()

        return true
    }


    override suspend fun isPackageInCargoQueue(
        warehouseId: String,
        packageId: String
    ): Boolean {

        val warehouse =
            getById(warehouseId)
                ?: return false


        return warehouse
            .getCargoQueue()
            .any {
                it.id == packageId
            }
    }


    override suspend fun getAllWarehouseServices():
            List<WarehouseServices> {

        return getAllWarehouses()
            .getOrThrow()
            .map {

                WarehouseServices(
                    warehouseId = it.id,
                    supportsFragileHandling = Random.nextBoolean(),
                    supportsColdStorage = Random.nextBoolean(),
                    supportsSpecialHandling = Random.nextBoolean()
                )
            }
    }

    @Suppress("ReturnCount")
    override suspend fun getById(
        id: String
    ): Warehouse? {


        val cached =
            warehouses.firstOrNull {
                it.id == id
            }


        if (cached != null) {
            return cached
        }


        val remoteDto =
            dependencies.remoteDataSource
                .getById(id)
                ?: return null


        val warehouse =
            mapWarehouseSafely(remoteDto)


        warehouse?.let {
            warehouses.add(it)
        }


        return warehouse
    }


    override suspend fun save(
        warehouse: Warehouse
    ): Warehouse {


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


        return savedWarehouse
    }


    override suspend fun update(
        id: String,
        name: String?,
        regionalZone: RegionalZone?,
        latitude: Double?,
        longitude: Double?
    ): Warehouse {


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
                .update(
                    id = id,
                    request = requestDto
                )


        val updatedWarehouse =
            mapWarehouseSafely(responseDto)
                ?: error(
                    "Warehouse update failed."
                )


        warehouses.removeIf {
            it.id == id
        }


        warehouses.add(
            updatedWarehouse
        )


        return updatedWarehouse
    }


    override suspend fun delete(
        id: String
    ): Boolean {

        val deleted =
            dependencies.remoteDataSource
                .delete(id)


        if (deleted) {

            warehouses.removeIf {
                it.id == id
            }
        }


        return deleted
    }
}

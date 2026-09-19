package org.example.data.repositoryImplementation

import org.example.data.dataholder.WareHouseRaw
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


    @Suppress("TooGenericExceptionCaught")
    override fun getAllWarehouses(): Result<List<Warehouse>> {
        return runCatching {
            val rawResults = dependencies.localDataSource.getWarehouses()
            val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
            val rawWarehouses =
                rawResults
                    .mapNotNull { it.rawData }
            rawWarehouses.mapNotNull { raw ->
                mapValidWarehouse(
                    raw,
                    warnings
                )
            }
        }
    }


    private fun mapValidWarehouse(
        raw: WareHouseRaw,
        warnings: MutableList<String>
    ): Warehouse? {


        val validation =
            dependencies.validator.validate(raw)


        if (validation.isNotEmpty()) {

            warnings.addAll(validation)

            return null
        }


        return dependencies.localMapper.map(raw)
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


    override fun getAllWarehouseServices():
            List<WarehouseServices> {

        return getAllWarehouses()
            .getOrThrow()
            .map { warehouse ->

                WarehouseServices(
                    warehouseId = warehouse.id,
                    supportsFragileHandling = Random.nextBoolean(),
                    supportsColdStorage = Random.nextBoolean(),
                    supportsSpecialHandling = Random.nextBoolean()
                )
            }
    }

    override suspend fun getById(
        id: String
    ): Warehouse? {

        val remoteDto =
            dependencies.remoteDataSource
                .getById(id)

        if (remoteDto != null) {
            return dependencies.remoteMapper
                .mapToDomainModel(remoteDto)
        }

        return getAllWarehouses()
            .getOrThrow()
            .firstOrNull {
                it.id == id
            }
    }

    override suspend fun save(
        warehouse: Warehouse
    ): Warehouse {


        val requestDto =
            dependencies.remoteMapper
                .mapToCreateRequest(warehouse)


        val responseDto =
            dependencies.remoteDataSource
                .save(requestDto)


        return dependencies.remoteMapper
            .mapToDomainModel(responseDto)
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


        return dependencies.remoteMapper
            .mapToDomainModel(responseDto)
    }


    override suspend fun delete(
        id: String
    ): Boolean {

        return dependencies.remoteDataSource
            .delete(id)
    }
}

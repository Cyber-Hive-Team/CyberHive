package org.example.data.repositoryImplementation

import org.example.data.dataholder.WareHouseRaw
import org.example.data.repositoryImplementation.dependencies.WarehouseRepositoryDependencies
import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.result.Result
import org.example.domain.repository.WarehouseRepository
import kotlin.random.Random


class WarehouseRepositoryImpl(
    private val dependencies: WarehouseRepositoryDependencies
) : WarehouseRepository {


    @Suppress("TooGenericExceptionCaught")
    override fun getAllWarehouses(): Result<List<Warehouse>> {
        return try {
            val rawResults = dependencies.localDataSource.getWarehouses()
            val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
            val rawWarehouses = rawResults.mapNotNull { it.rawData }
            val warehouses = rawWarehouses.mapNotNull { raw ->
                mapValidWarehouse(raw, warnings)
                }
            Result(
                data = warehouses,
                errorMessage =
                    warnings
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("; ")
            )

        } catch (e: Exception) {
            Result(
                data = emptyList(),
                errorMessage =
                    "Failed to load warehouses: ${e.message}"
            )
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


    override fun getWarehouseById(
        warehouseId: String
    ): Warehouse? {

        return getAllWarehouses()
            .data
            .firstOrNull {
                it.id == warehouseId
            }
    }


    override fun addPackageToCargoQueue(
        warehouseId: String,
        cargoPackage: Package
    ): Boolean {

        val warehouse =
            getWarehouseById(warehouseId)
                ?: return false


        warehouse.addPackages(
            listOf(cargoPackage)
        )


        return true
    }


    override fun sortCargoQueue(
        warehouseId: String
    ): Boolean {

        val warehouse =
            getWarehouseById(warehouseId)
                ?: return false


        warehouse.sortCargoQueue()


        return true
    }


    override fun isPackageInCargoQueue(
        warehouseId: String,
        packageId: String
    ): Boolean {

        val warehouse =
            getWarehouseById(warehouseId)
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
            .data
            .map { warehouse ->

                WarehouseServices(
                    warehouseId = warehouse.id,
                    supportsFragileHandling = Random.nextBoolean(),
                    supportsColdStorage = Random.nextBoolean(),
                    supportsSpecialHandling = Random.nextBoolean()
                )
            }
    }


    override suspend fun getRemoteById(
        id: String
    ): Warehouse? {


        val responseDto =
            dependencies.remoteDataSource
                .getById(id)
                ?: return null


        return dependencies.remoteMapper
            .mapToDomainModel(responseDto)
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

package org.example.data.repository

import org.example.data.dataholder.WareHouseRaw
import org.example.data.datasource.WarehouseDataSource
import org.example.data.mapper.WarehouseMapper
import org.example.data.validation.WarehouseValidator
import org.example.domain.model.Package
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.result.Result
import org.example.domain.repository.WarehouseRepository
import kotlin.random.Random

class CsvWarehouseRepository(
    private val dataSource: WarehouseDataSource,
    private val mapper: WarehouseMapper,
    private val validator: WarehouseValidator

) : WarehouseRepository {

    override fun getAllWarehouses(): Result<List<Warehouse>> {
        val rawResults = dataSource.getWarehouses()
        val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
        val rawWarehouses = rawResults.mapNotNull { it.rawData }
        val warehouses = rawWarehouses.mapNotNull { raw ->
            mapValidWarehouse(raw, warnings)
        }
        return Result(
            data = warehouses,
            errorMessage = warnings
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )

    }

    private fun mapValidWarehouse(
        raw: WareHouseRaw,
        warnings: MutableList<String>
    ): Warehouse? {
        val validation = validator.validate(raw)

        if (validation.isNotEmpty()) {
            warnings.addAll(validation)
            return null
        }

        return mapper.map(raw)

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

        val warehouse = getWarehouseById(warehouseId)
            ?: return false

        return warehouse.getCargoQueue()
            .any { it.id == packageId }

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

}



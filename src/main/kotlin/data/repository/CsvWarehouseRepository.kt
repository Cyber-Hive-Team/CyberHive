package org.example.data.repository

import org.example.data.datasource.WarehouseDataSource
import org.example.data.mapper.WarehouseMapper
import org.example.domain.model.Warehouse
import org.example.domain.repository.Result
import org.example.domain.repository.WarehouseRepository

class CsvWarehouseRepository(
    private val dataSource: WarehouseDataSource,
    private val mapper: WarehouseMapper
) : WarehouseRepository {


    override fun getAllWarehouses(): Result<List<Warehouse>> {
        val dataSourceResult = dataSource.getWarehouses()

        val warehouses = mutableListOf<Warehouse>()
        val warnings = dataSourceResult.warnings.toMutableList()

        for (rawWarehouse in dataSourceResult.warehouses) {
            val mappingResult = mapper.map(rawWarehouse)

            warnings.addAll(mappingResult.warnings)

            mappingResult.warehouse?.let { warehouse ->
                warehouses.add(warehouse)
            }
        }

        val errorMessage = if (warnings.isEmpty()) {
            null
        } else {
            warnings.joinToString("; ")
        }

        return Result(warehouses, errorMessage)
    }
}
package org.example.data.repository

import org.example.data.datasource.WarehouseDataSource
import org.example.data.mapper.WarehouseMapper
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository
import org.example.domain.repository.WarehouseRepositoryResult

class CsvWarehouseRepository(
    private val dataSource: WarehouseDataSource,
    private val mapper: WarehouseMapper
) : WarehouseRepository {


    override fun getAllWarehouses(): WarehouseRepositoryResult {
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

        return WarehouseRepositoryResult(
            warehouses = warehouses,
            warnings = warnings
        )
    }
}
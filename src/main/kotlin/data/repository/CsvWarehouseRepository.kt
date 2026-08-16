package org.example.data.repository

import org.example.data.dataholder.WareHouseRaw
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
        val result = dataSource.getWarehouses()
        val warehouses = mutableListOf<Warehouse>()
        val warnings = result.warnings.toMutableList()

        result.warehouses.forEach { raw ->
            val validationWarnings = validate(raw)

            if (validationWarnings.isEmpty()) {
                warehouses.add(mapper.map(raw))
            } else {
                warnings.addAll(validationWarnings)
            }
        }

        val errorMessage = warnings
            .takeIf { it.isNotEmpty() }
            ?.joinToString("; ")

        return Result(
            warehouses,
            errorMessage
        )
    }

    private fun validate(
        raw: WareHouseRaw
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add(
                "Warning: Warehouse skipped - ID is missing"
            )
        }

        return warnings
    }
}
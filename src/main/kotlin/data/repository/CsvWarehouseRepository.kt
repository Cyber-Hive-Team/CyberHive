package org.example.data.repository

import org.example.data.dataholder.WareHouseRaw
import org.example.data.datasource.WarehouseDataSource
import org.example.data.mapper.WarehouseMapper
import org.example.domain.model.Warehouse
import org.example.domain.repository.Result
import org.example.domain.repository.WarehouseRepository

private const val MIN_VALID_COORDINATE = 0.0

class CsvWarehouseRepository(
    private val dataSource: WarehouseDataSource,
    private val mapper: WarehouseMapper
) : WarehouseRepository {

    override fun getAllWarehouses(): Result<List<Warehouse>> {
        val result = dataSource.getWarehouses()
        val warnings = result.warnings.toMutableList()

        val warehouses = result.warehouses.mapNotNull { raw ->
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
        val validation = validate(raw)

        if (validation.isNotEmpty()) {
            warnings.addAll(validation)
            return null
        }

        return mapper.map(raw)
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

        if (raw.latitude < MIN_VALID_COORDINATE) {
            warnings.add(
                "Warning: Warehouse ${raw.id} skipped - invalid latitude"
            )
        }

        if (raw.longitude < MIN_VALID_COORDINATE) {
            warnings.add(
                "Warning: Warehouse ${raw.id} skipped - invalid longitude"
            )
        }

        return warnings
    }
}

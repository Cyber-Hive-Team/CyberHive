package org.example.data.datasource

import org.example.data.dataholder.WareHouseRaw
import org.example.data.dataholder.WarehouseDataSourceResult
import org.example.data.dataparsing.convertCsvRowToWarehouseRawObject
import java.io.File

private const val FIRST_DATA_ROW_INDEX = 1

class CsvWarehouseDataSource(
    private val filePath: String
) : WarehouseDataSource {

    override fun getWarehouses(): WarehouseDataSourceResult {
        val warnings = mutableListOf<String>()
        val rows = readAllLines(warnings)

        if (rows.isEmpty()) {
            return WarehouseDataSourceResult(
                warehouses = emptyList(),
                warnings = warnings
            )
        }

        val warehouses = mutableListOf<WareHouseRaw>()

        for (index in FIRST_DATA_ROW_INDEX until rows.size) {
            val rawWarehouse = convertCsvRowToWarehouseRawObject(
                row = rows[index].trim(),
                rowIndex = index,
                warnings = warnings
            )

            if (rawWarehouse != null) {
                warehouses.add(rawWarehouse)
            }
        }

        return WarehouseDataSourceResult(
            warehouses = warehouses,
            warnings = warnings
        )
    }

    private fun readAllLines(
        warnings: MutableList<String>
    ): List<String> {
        val file = File(filePath)

        if (!file.exists()) {
            warnings.add(
                "Warning: warehouses.csv was not found at: $filePath"
            )
            return emptyList()
        }

        return file.readLines()
    }
}
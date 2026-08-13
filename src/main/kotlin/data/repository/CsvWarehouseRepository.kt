package org.example.data.repository

import org.example.data.dataparsing.convertCsvRowToWarehouseRawObject
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository
import org.example.domain.repository.WarehouseRepositoryResult
import java.io.File

private const val FIRST_DATA_ROW_INDEX = 1

class CsvWarehouseRepository(private val filePath: String) : WarehouseRepository {

    override fun getAllWarehouses(): WarehouseRepositoryResult {
        val warnings = mutableListOf<String>()

        val rows = readAllLinesFromFile(warnings)

        if (rows.isEmpty()) {
            return WarehouseRepositoryResult(emptyList(), warnings)
        }

        val warehouses = mutableListOf<Warehouse>()

        for (index in FIRST_DATA_ROW_INDEX until rows.size) {
            val currentRow = rows[index]

            val cleanedRow = currentRow.trim()

            val rawWarehouse = convertCsvRowToWarehouseRawObject(cleanedRow, index, warnings)

            if (rawWarehouse != null) {
                val domainWarehouse = Warehouse(
                    id = rawWarehouse.id,
                    name = rawWarehouse.name,
                    regionalZone = rawWarehouse.regionalZone,
                    latitude = rawWarehouse.latitude,
                    longitude = rawWarehouse.longitude
                )
                warehouses.add(domainWarehouse)
            }
        }

        return WarehouseRepositoryResult(warehouses, warnings)
    }

    private fun readAllLinesFromFile(warnings: MutableList<String>): List<String> {
        val file = File(filePath)
        if (!file.exists()) {
            warnings.add("Warning: File not found at path: $filePath")
            return emptyList()
        }
        return file.readLines()
    }
}

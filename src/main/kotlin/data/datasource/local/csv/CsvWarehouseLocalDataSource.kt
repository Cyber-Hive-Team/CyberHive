package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.WarehouseRaw
import org.example.data.dataparsing.convertCsvRowToWarehouseRawObject
import org.example.data.datasource.WarehouseDataSource
import org.example.data.exception.FileNotFoundDataException
import java.io.File

private const val FIRST_DATA_ROW_INDEX = 1

class CsvWarehouseLocalDataSource(
    private val filePath: String
) : WarehouseDataSource {

    override fun getWarehouses(): List<RawResult<WarehouseRaw>> {
        val rows = readAllLines()

        val rawWarehousesResultList =
            mutableListOf<RawResult<WarehouseRaw>>()

        for (index in FIRST_DATA_ROW_INDEX until rows.size) {

            val rawWarehouseResult = runCatching {
                convertCsvRowToWarehouseRawObject(
                    row = rows[index].trim(),
                    rowIndex = index
                )
            }.getOrElse { exception ->
                RawResult<WarehouseRaw>(
                    rawData = null,
                    errorMessage = exception.message
                )
            }

            rawWarehousesResultList.add(rawWarehouseResult)
        }

        return rawWarehousesResultList
    }

    private fun readAllLines(): List<String> {
        val file = File(filePath)

        if (!file.exists()) {
            throw FileNotFoundDataException(
                "Warehouse file not found: $filePath"
            )
        }

        return file.readLines()
    }

    override fun replaceAll(
        warehouses: List<WarehouseRaw>
    ) {
        val content = buildString {
            appendLine(
                "id,name,regionalZone,latitude,longitude"
            )

            warehouses.forEach { warehouse ->
                appendLine(
                    listOf(
                        warehouse.id,
                        warehouse.name,
                        warehouse.regionalZone.name,
                        warehouse.latitude,
                        warehouse.longitude
                    ).joinToString(",")
                )
            }
        }

        replaceCsvFile(
            filePath = filePath,
            content = content
        )
    }

}

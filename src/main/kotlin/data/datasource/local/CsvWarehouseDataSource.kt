package org.example.data.datasource.local

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.WareHouseRaw
import org.example.data.dataparsing.convertCsvRowToWarehouseRawObject
import org.example.data.datasource.WarehouseDataSource
import org.example.data.exception.FileNotFoundDataException
import java.io.File

private const val FIRST_DATA_ROW_INDEX = 1

class CsvWarehouseDataSource(
    private val filePath: String
) : WarehouseDataSource {

    override fun getWarehouses(): List<RawResult<WareHouseRaw>> {
        val rows = readAllLines()
        val rawWarehousesResultList = mutableListOf<RawResult<WareHouseRaw>>()

        for (index in FIRST_DATA_ROW_INDEX until rows.size) {
            val rawWarehouseResult = convertCsvRowToWarehouseRawObject(
                row = rows[index].trim(),
                rowIndex = index
            )
            rawWarehousesResultList.add(rawWarehouseResult)

        }
        return rawWarehousesResultList

    }

    private fun readAllLines(
    ): List<String> {
        val file = File(filePath)

        if (!file.exists()) {
            throw FileNotFoundDataException("Package file not found: $filePath")
        }

        return file.readLines()
    }

}

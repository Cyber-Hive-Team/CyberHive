package org.example.data.dataparsing

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.WareHouseRaw
import org.example.domain.model.RegionalZone

private const val REQUIRED_COLUMNS_COUNT = 5
private const val ID_INDEX = 0
private const val NAME_INDEX = 1
private const val ZONE_INDEX = 2
private const val LAT_INDEX = 3
private const val LON_INDEX = 4

fun convertCsvRowToWarehouseRawObject(row: String, rowIndex: Int): RawResult<WareHouseRaw> {
    val columns = row.split(",").map { it.trim() }

    if (!hasRequiredColumns(columns)) {
        return RawResult(
            rawData = null,
            errorMessage = "Row ${rowIndex + 1} skipped - missing columns"
        )
    }

    val zone = convertToZone(columns[ZONE_INDEX])
    if (zone == null) {
        return RawResult(
            rawData = null,
            errorMessage = "Row ${rowIndex + 1} skipped - invalid zone: ${columns[ZONE_INDEX]}"
        )
    }
    val warehouseRaw = extractWarehouseRaw(columns, zone)

    return RawResult(
        rawData = warehouseRaw,
        errorMessage = null
    )
}


private fun hasRequiredColumns(columns: List<String>): Boolean {
    return columns.size >= REQUIRED_COLUMNS_COUNT

}

private fun convertToZone(zoneText: String): RegionalZone? {
    return RegionalZone.entries.find {
        it.name.equals(zoneText, ignoreCase = true)
    }
}


private fun extractWarehouseRaw(columns: List<String>, zone: RegionalZone): WareHouseRaw {
    return WareHouseRaw(
        id = columns[ID_INDEX].uppercase(),
        name = columns[NAME_INDEX],
        regionalZone = zone,
        latitude = parseCoordinate(columns[LAT_INDEX]),
        longitude = parseCoordinate(columns[LON_INDEX])
    )
}

private fun parseCoordinate(value: String): Double {
    if (value.isBlank() || value.equals("null", true) || value.equals("N/A", true)) {
        return -1.0
    }
    return value.toDoubleOrNull() ?: -1.0
}

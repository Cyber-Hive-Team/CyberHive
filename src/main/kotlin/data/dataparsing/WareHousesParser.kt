package org.example.data.dataparsing

import org.example.data.dataholder.WareHouseRaw
import org.example.domain.model.RegionalZone

private const val REQUIRED_COLUMNS_COUNT = 5
private const val ID_INDEX = 0
private const val NAME_INDEX = 1
private const val ZONE_INDEX = 2
private const val LAT_INDEX = 3
private const val LON_INDEX = 4

fun convertCsvRowToWarehouseRawObject(row: String, rowIndex: Int, warnings: MutableList<String>): WareHouseRaw? {
    val columns = row.split(",").map { it.trim() }

    if (!hasRequiredColumns(columns, rowIndex, warnings)) return null

    val zone = convertToZone(columns[ZONE_INDEX], rowIndex, warnings) ?: return null

    return extractWarehouseRaw(columns, zone)
}

private fun hasRequiredColumns(columns: List<String>, rowIndex: Int, warnings: MutableList<String>): Boolean {
    if (columns.size < REQUIRED_COLUMNS_COUNT) {
        warnings.add("Warning: Row ${rowIndex + 1} skipped - missing columns")
        return false
    }
    return true
}

private fun convertToZone(zoneText: String, rowIndex: Int, warnings: MutableList<String>): RegionalZone? {
    val zone = RegionalZone.entries.find { it.name.equals(zoneText, ignoreCase = true) }
    if (zone == null) {
        warnings.add("Warning: Row ${rowIndex + 1} skipped - invalid zone: $zoneText")
    }
    return zone
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

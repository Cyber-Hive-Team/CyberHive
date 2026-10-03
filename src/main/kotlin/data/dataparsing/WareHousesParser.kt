package org.example.data.dataparsing

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.WarehouseRaw
import org.example.data.exception.InvalidColumnCountException
import org.example.domain.model.RegionalZone

private const val REQUIRED_COLUMNS_COUNT = 5
private const val ID_INDEX = 0
private const val NAME_INDEX = 1
private const val ZONE_INDEX = 2
private const val LAT_INDEX = 3
private const val LON_INDEX = 4

private val WAREHOUSE_ID_REGEX = Regex("^WH-\\d{3}$")

fun convertCsvRowToWarehouseRawObject(
    row: String,
    rowIndex: Int
): RawResult<WarehouseRaw> {
    val columns = row.split(",").map { it.trim() }

    validateColumns(columns, rowIndex)

    val warehouseId = parseWarehouseId(columns[ID_INDEX])
    val zone = convertToZone(columns[ZONE_INDEX])

    return when {
        warehouseId == null -> invalidWarehouseIdResult(columns, rowIndex)
        zone == null -> invalidZoneResult(columns, rowIndex)
        else -> createWarehouseResult(columns, warehouseId, zone, rowIndex)
    }
}

private fun validateColumns(
    columns: List<String>,
    rowIndex: Int
) {
    if (!hasRequiredColumns(columns)) {
        throw InvalidColumnCountException(
            "Row ${rowIndex + 1} skipped - missing columns"
        )
    }
}

private fun parseWarehouseId(value: String): String? {
    val warehouseId = value.uppercase()

    return warehouseId.takeIf {
        it.matches(WAREHOUSE_ID_REGEX)
    }
}

private fun invalidWarehouseIdResult(
    columns: List<String>,
    rowIndex: Int
): RawResult<WarehouseRaw> {
    return RawResult(
        rawData = null,
        errorMessage =
            "Row ${rowIndex + 1} skipped - invalid warehouse id: '${columns[ID_INDEX]}'"
    )
}

private fun invalidZoneResult(
    columns: List<String>,
    rowIndex: Int
): RawResult<WarehouseRaw> {
    return RawResult(
        rawData = null,
        errorMessage =
            "Row ${rowIndex + 1} skipped - invalid zone: ${columns[ZONE_INDEX]}"
    )
}

private fun createWarehouseResult(
    columns: List<String>,
    warehouseId: String,
    zone: RegionalZone,
    rowIndex: Int
): RawResult<WarehouseRaw> {
    val warehouseRaw = extractWarehouseRaw(
        columns = columns,
        warehouseId = warehouseId,
        zone = zone
    )

    return if (
        warehouseRaw.latitude == null ||
        warehouseRaw.longitude == null
    ) {
        RawResult(
            rawData = null,
            errorMessage =
                "Row ${rowIndex + 1} skipped - missing or invalid coordinates"
        )
    } else {
        RawResult(
            rawData = warehouseRaw,
            errorMessage = null
        )
    }
}

private fun hasRequiredColumns(columns: List<String>): Boolean {
    return columns.size >= REQUIRED_COLUMNS_COUNT
}

private fun convertToZone(zoneText: String): RegionalZone? {
    return RegionalZone.entries.find {
        it.name.equals(zoneText, ignoreCase = true)
    }
}

private fun extractWarehouseRaw(
    columns: List<String>,
    warehouseId: String,
    zone: RegionalZone
): WarehouseRaw {
    return WarehouseRaw(
        id = warehouseId,
        name = columns[NAME_INDEX],
        regionalZone = zone,
        latitude = parseCoordinate(columns[LAT_INDEX]),
        longitude = parseCoordinate(columns[LON_INDEX])
    )
}

private fun parseCoordinate(value: String): Double? {
    if (
        value.isBlank() ||
        value.equals("null", true) ||
        value.equals("N/A", true)
    ) {
        return null
    }

    return value.toDoubleOrNull()
}

package org.example.data.dataparsing

import org.example.data.dataholder.VehicleParseResult
import org.example.data.dataholder.VehicleRaw
import java.io.File

private const val FIRST_DATA_ROW = 1
private const val REQUIRED_COLUMNS = 4
private const val ID_INDEX = 0
private const val HUB_INDEX = 1
private const val CAPACITY_INDEX = 2
private const val COST_INDEX = 3
private const val INVALID_VALUE = -1.0

fun parseVehicles(filePath: String): VehicleParseResult {
    val lines = File(filePath).readLines()
    val warnings = mutableListOf<String>()
    val vehicles = parseRows(lines, warnings)

    return VehicleParseResult(vehicles, warnings)
}

private fun parseRows(
    lines: List<String>,
    warnings: MutableList<String>
): List<VehicleRaw> {
    return lines.drop(FIRST_DATA_ROW)
        .mapIndexedNotNull { index, line ->
            parseLine(line, index + FIRST_DATA_ROW, warnings)
        }
}

private fun parseLine(
    line: String,
    lineNumber: Int,
    warnings: MutableList<String>
): VehicleRaw? {
    val columns = line.split(",").map { it.trim() }

    if (columns.size < REQUIRED_COLUMNS) {
        warnings.add("Invalid vehicle row: $lineNumber")
        return null
    }

    return parseFleetRow(
        columns[ID_INDEX],
        columns[HUB_INDEX],
        columns[CAPACITY_INDEX],
        columns[COST_INDEX]
    )
}

private fun parseFleetRow(
    vehicleId: String,
    currentHubId: String,
    capacity: String,
    cost: String
): VehicleRaw? {
    if (vehicleId.isBlank() || currentHubId.isBlank()) {
        return null
    }

    return VehicleRaw(
        id = vehicleId,
        currentHubId = currentHubId,
        maxCapacityKg = parseNumericValue(capacity),
        costPerKm = parseNumericValue(cost)
    )
}

private fun parseNumericValue(value: String): Double =
    value.toDoubleOrNull() ?: INVALID_VALUE
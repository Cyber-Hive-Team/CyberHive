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
    val vehicles = mutableListOf<VehicleRaw>()
    val warnings = mutableListOf<String>()

    lines.drop(FIRST_DATA_ROW).forEachIndexed { index, line ->
        parseLine(line, index + FIRST_DATA_ROW, warnings)?.let {
            vehicles.add(it)
        }
    }

    return VehicleParseResult(vehicles, warnings)
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
        vehicleId = columns[ID_INDEX],
        currentHubId = columns[HUB_INDEX],
        maxCapacityKgValue = columns[CAPACITY_INDEX],
        costPerKmValue = columns[COST_INDEX]
    )
}

fun parseFleetRow(
    vehicleId: String,
    currentHubId: String,
    maxCapacityKgValue: String,
    costPerKmValue: String
): VehicleRaw? {
    if (vehicleId.isBlank() || currentHubId.isBlank()) {
        return null
    }

    return VehicleRaw(
        id = vehicleId,
        currentHubId = currentHubId,
        maxCapacityKg = parseNumericValue(maxCapacityKgValue),
        costPerKm = parseNumericValue(costPerKmValue)
    )
}

private fun parseNumericValue(value: String): Double {
    return value.toDoubleOrNull() ?: INVALID_VALUE
}
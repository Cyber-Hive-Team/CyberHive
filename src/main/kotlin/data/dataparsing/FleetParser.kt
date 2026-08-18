package org.example.data.dataparsing

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.VehicleRaw
import java.io.File

private const val FIRST_DATA_ROW = 1
private const val REQUIRED_COLUMNS = 4
private const val ID_INDEX = 0
private const val HUB_INDEX = 1
private const val CAPACITY_INDEX = 2
private const val COST_INDEX = 3
private const val INVALID_VALUE = -1.0

fun parseVehicles(filePath: String): List<RawResult<VehicleRaw>> {
    val lines = File(filePath).readLines()

    val rawVehiclesResultList: List<RawResult<VehicleRaw>> = parseRows(lines)
    return rawVehiclesResultList
}

private fun parseRows(
    lines: List<String>,
): List<RawResult<VehicleRaw>> {

    return lines.drop(FIRST_DATA_ROW)
        .mapIndexed { index, line ->
            parseLine(
                line = line,
                lineNumber = index + FIRST_DATA_ROW
            )
        }

}

private fun parseLine(
    line: String,
    lineNumber: Int,
): RawResult<VehicleRaw> {
    val columns = line.split(",").map { it.trim() }

    if (columns.size < REQUIRED_COLUMNS) {
        return RawResult(
            rawData = null,
            errorMessage = "Invalid vehicle row: $lineNumber"
        )
    }
    val vehicleItem = parseFleetRow(
        columns[ID_INDEX],
        columns[HUB_INDEX],
        columns[CAPACITY_INDEX],
        columns[COST_INDEX]
    )
    if (vehicleItem == null) {
        return RawResult(
            rawData = null,
            errorMessage = "Vehicle row $lineNumber has missing required fields"
        )
    }
    return RawResult(
        rawData = vehicleItem,
        errorMessage = null
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
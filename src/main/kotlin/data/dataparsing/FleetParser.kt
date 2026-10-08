package org.example.data.dataparsing

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.VehicleRaw
import org.example.data.exception.InvalidColumnCountException
import org.example.data.exception.MissingRequiredFieldException
import java.io.File

private const val FIRST_DATA_ROW = 1
private const val REQUIRED_COLUMNS = 4
private const val ID_INDEX = 0
private const val HUB_INDEX = 1
private const val CAPACITY_INDEX = 2
private const val COST_INDEX = 3

private val VEHICLE_ID_REGEX = Regex("^TRK-\\d{4}$")

fun parseVehicles(filePath: String): List<RawResult<VehicleRaw>> {
    val lines = File(filePath).readLines()

    return parseRows(lines)
}

private fun parseRows(
    lines: List<String>,
): List<RawResult<VehicleRaw>> {

    return lines
        .drop(FIRST_DATA_ROW)
        .mapIndexed { index, line ->
            runCatching {
                parseLine(
                    line = line,
                    lineNumber = index + FIRST_DATA_ROW
                )
            }.getOrElse { exception ->
                RawResult<VehicleRaw>(
                    rawData = null,
                    errorMessage = exception.message
                )
            }
        }
}

private fun parseLine(
    line: String,
    lineNumber: Int,
): RawResult<VehicleRaw> {

    val columns = line
        .split(",")
        .map { it.trim() }

    if (columns.size < REQUIRED_COLUMNS) {
        throw InvalidColumnCountException(
            "Invalid vehicle row: $lineNumber"
        )
    }

    val vehicleId = columns[ID_INDEX].uppercase()

    if (!vehicleId.matches(VEHICLE_ID_REGEX)) {
        return RawResult(
            rawData = null,
            errorMessage =
                "Vehicle row $lineNumber has invalid vehicle id: '$vehicleId'"
        )
    }

    val vehicleItem = parseFleetRow(
        vehicleId = vehicleId,
        currentHubId = columns[HUB_INDEX],
        capacity = columns[CAPACITY_INDEX],
        cost = columns[COST_INDEX]
    )

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
): VehicleRaw {

    if (vehicleId.isBlank() || currentHubId.isBlank()) {
        throw MissingRequiredFieldException(
            "Vehicle row $vehicleId has missing required fields"
        )
    }

    return VehicleRaw(
        id = vehicleId,
        currentHubId = currentHubId,
        maxCapacityKg = parseCapacity(capacity),
        costPerKm = parseCost(cost)
    )
}

private fun parseCapacity(value: String): Double? =
    value.toDoubleOrNull()

private fun parseCost(value: String): Double =
    value.toDoubleOrNull()
        ?: throw MissingRequiredFieldException(
            "Vehicle costPerKm is missing or invalid"
        )


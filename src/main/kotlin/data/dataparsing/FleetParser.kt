package org.example.data.dataparsing

import org.example.data.dataholder.VehicleParseResult
import org.example.data.dataholder.VehicleRaw
import java.io.File

private const val FIRST_DATA_ROW_INDEX = 1
private const val EXPECTED_COLUMN_COUNT = 4

fun parseVehicles(filePath: String): VehicleParseResult {
    val vehicles = mutableListOf<VehicleRaw>()
    val warnings = mutableListOf<String>()

    val file = File(filePath)

    if (!file.exists()) {
        warnings.add(
            "Warning: fleet.csv was not found at: $filePath"
        )

        return VehicleParseResult(
            vehicles = emptyList(),
            warnings = warnings
        )
    }

    val rows = file.readLines()

    for (index in FIRST_DATA_ROW_INDEX until rows.size) {
        val columns = rows[index]
            .split(",")
            .map { it.trim() }

        if (columns.size < EXPECTED_COLUMN_COUNT) {
            warnings.add(
                "Warning: Row ${index + 1} skipped - " +
                        "missing columns"
            )
            continue
        }

        val vehicle = parseFleetRow(
            vehicleId = columns[0],
            currentHubId = columns[1],
            maxCapacityKgValue = columns[2],
            costPerKmValue = columns[3]
        )

        if (vehicle == null) {
            warnings.add(
                "Warning: Row ${index + 1} skipped - " +
                        "invalid vehicle data"
            )
        } else {
            vehicles.add(vehicle)
        }
    }

    return VehicleParseResult(
        vehicles = vehicles,
        warnings = warnings
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

    val maxCapacityKg = parseNumericValue(maxCapacityKgValue)
    val costPerKm = parseNumericValue(costPerKmValue)

    return VehicleRaw(
        vehicleId,
        currentHubId,
        maxCapacityKg,
        costPerKm
    )
}

private fun parseNumericValue(
    value: String
): Double {
    var number = -1.0

    if (value.isNotBlank()) {
        number = value.toDoubleOrNull() ?: -1.0
    }

    return number
}
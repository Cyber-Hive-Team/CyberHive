package org.example.data.dataparsing

import org.example.data.dataholder.VehicleRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

fun readFleetLines(filePath: String): List<String> {
    val fleetFilePath = Path(filePath)
    if (!fleetFilePath.exists()) {
        println("Warning: fleet.csv was not found.")
        return emptyList()
    }
    return fleetFilePath.readLines()
}

fun parseFleet(filePath: String): List<VehicleRaw> {
    val fleetLines = readFleetLines(filePath)
    val firstDataLineIndex = 1

    if (fleetLines.size <= firstDataLineIndex) {
        return emptyList()
    }

    return fleetLines.subList(firstDataLineIndex, fleetLines.size)
        .mapIndexedNotNull { index, line -> parseFleetRow(line, firstDataLineIndex + index + 1) }
}

private fun parseFleetRow(line: String, csvLineNumber: Int): VehicleRaw? {
    val totalExpectedColumns = 4

    if (line.isBlank()) {
        return null
    }

    val columns = line.split(",")
    if (columns.size != totalExpectedColumns) {
        println("Warning: fleet row $csvLineNumber was skipped because the number of columns is invalid.")
        return null
    }

    val vehicleId = cleanFleetId(columns[0], "vehicle ID", csvLineNumber)
    val currentHubId = cleanFleetId(columns[1], "current hub ID", csvLineNumber)

    if (vehicleId.isBlank() || currentHubId.isBlank()) {
        return null
    }

    val maxCapacityKg = cleanNumericField(columns[2])
    val costPerKm = cleanNumericField(columns[3])

    return VehicleRaw(vehicleId, currentHubId, maxCapacityKg, costPerKm)
}

fun cleanFleetId(id: String, fieldName: String, csvLineNumber: Int): String {
    val cleanedId = id.trim().uppercase()
    if (cleanedId.isBlank()) {
        println("Warning: fleet row $csvLineNumber was skipped because $fieldName is missing.")
    }
    return cleanedId
}

fun cleanNumericField(value: String): Double {
    val cleaned = value.trim()
    if (cleaned.isBlank() || cleaned.equals("N/A", ignoreCase = true) || cleaned.equals("null", ignoreCase = true)) {
        return -1.0
    }
    return cleaned.toDoubleOrNull() ?: -1.0
}
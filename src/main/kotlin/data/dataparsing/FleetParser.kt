package org.example.data.dataparsing
import org.example.data.dataholder.FleetRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
fun readFleetLines(): List<String> {
    val fleetFilePath = Path("src/main/resources/fleet.csv")
    if (!fleetFilePath.exists()) {
        println("Warning: fleet.csv was not found.")
        return emptyList()
    }
    return fleetFilePath.readLines()
}
fun parseFleet(): List<FleetRaw> {
    val fleet = mutableListOf<FleetRaw>()
    val fleetLines = readFleetLines()
    val firstDataLineIndex = 1
    val totalExpectedColumns = 4
    // Start from index 1 to skip the CSV header row
    for (lineIndex in firstDataLineIndex until fleetLines.size) {
        val currentFleetLine = fleetLines[lineIndex]
        val csvLineNumber = lineIndex + 1     // Add 1 to get the exact line position in the CSV file
        if (currentFleetLine.isBlank()) {
            continue
        }
        val fleetColumns = currentFleetLine.split(",")
        if (fleetColumns.size != totalExpectedColumns) {
            println(
                "Warning: fleet row $csvLineNumber was skipped because the number of columns is invalid.")
            continue
        }
        val vehicleId = cleanFleetId(fleetColumns[0], "vehicle ID", csvLineNumber)
        val currentHubId = cleanFleetId(fleetColumns[1], "current hub ID", csvLineNumber)
        if (vehicleId.isBlank() || currentHubId.isBlank()) {
            continue
        }
        val maxCapacityKg = cleanNumericField(fleetColumns[2])
        val costPerKm = cleanNumericField(fleetColumns[3])
        fleet.add(FleetRaw(vehicleId, currentHubId, maxCapacityKg, costPerKm))
    }
    return fleet
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



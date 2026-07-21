package org.example.DataParsing
import org.example.Data.RouteRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines
fun readRouteLines(): List<String> {
    val routeFilePath = Path("src/main/resources/routes.csv")
    if (!routeFilePath.exists()) {
        println("Warning: routes.csv was not found.")
        return emptyList()}
    else{
        return routeFilePath.readLines()}
}
fun parseRoutes(): List<RouteRaw> {
    val routes = mutableListOf<RouteRaw>()
    val linesRoutes = readRouteLines()
    val firstLine = 1
    for (line in firstLine until linesRoutes.size) {
        val currentRouteLine = linesRoutes[line]
        val csvLineNumber = line + 1       // Add 1 to get the exact line position in the CSV file
        if (currentRouteLine.isBlank()) {
            continue
        }
        val routeColumns = currentRouteLine.split(",")
        val totalExpectedColumns = 5
        if (routeColumns.size != totalExpectedColumns) {
            println("Warning: route row $csvLineNumber was skipped because the number of columns is invalid.")
            continue
        }
        val id = cleanId(routeColumns[0], "route ID", csvLineNumber)
        val originHubId = cleanId(routeColumns[1], "origin hub ID", csvLineNumber)
        val destinationHubId = cleanId(routeColumns[2], "destination hub ID", csvLineNumber)
        if (id.isBlank() || originHubId.isBlank() || destinationHubId.isBlank()) {
            continue
        }
        val distanceKm = cleanDistance(routeColumns[3])
        val typicalDelayMin = cleanDelay(routeColumns[4])
        routes.add(
            RouteRaw(id, originHubId, destinationHubId, distanceKm, typicalDelayMin)
        )
    }
    return routes
}
fun cleanId(id: String, fieldName: String, csvLineNumber: Int): String {
    if (id.isBlank()) {
        println("Warning: route row $csvLineNumber was skipped because $fieldName is missing.")
    }
    return id
}
fun cleanDistance(distanceBeforeCleaning: String): Double {
    val distanceAfterCleaning = distanceBeforeCleaning.replace("km", "", ignoreCase = true).trim()
    if (
        distanceAfterCleaning.isBlank() ||
        distanceAfterCleaning.equals("N/A", ignoreCase = true) ||
        distanceAfterCleaning.equals("null", ignoreCase = true)
    ) {
        return -1.0
    }
    return distanceAfterCleaning.toDoubleOrNull() ?: -1.0
}
fun cleanDelay(delayBeforeCleaning: String): Int {
    val delayAfterCleaning = delayBeforeCleaning.trim()
    if (
        delayAfterCleaning.isBlank() ||
        delayAfterCleaning.equals("N/A", ignoreCase = true) ||
        delayAfterCleaning.equals("null", ignoreCase = true)
    ) {
        return -1
    }
    return delayAfterCleaning.toIntOrNull() ?: -1
}

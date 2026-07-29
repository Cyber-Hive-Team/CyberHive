package org.example.dataparsing
import org.example.data.dataholder.RouteRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines


fun readRouteLines(): List<String> {
    val routeFilePath = Path("src/main/resources/routes.csv")
    val routeLines: List<String>
    if (!routeFilePath.exists()) {
        println("Warning: routes.csv was not found.")
        routeLines = emptyList()}
    else{
        routeLines= routeFilePath.readLines()}
    return routeLines

}
fun parseRoutes(): List<RouteRaw> {
    val routes = mutableListOf<RouteRaw>()
    val linesRoutes = readRouteLines()
    val firstLine = 1
    val totalExpectedColumns = 5
    for (line in firstLine until linesRoutes.size) {
        val currentRouteLine = linesRoutes[line]
        val csvLineNumber = line + 1
        if (currentRouteLine.isNotBlank()) {
            val routeColumns = currentRouteLine.split(",")
            if (routeColumns.size == totalExpectedColumns) {
                val id = cleanId(routeColumns[0], "route ID", csvLineNumber)
                val originHubId = cleanId(routeColumns[1], "origin hub ID", csvLineNumber)
                val destinationHubId = cleanId(routeColumns[2], "destination hub ID", csvLineNumber)
                if (id.isNotBlank() && originHubId.isNotBlank() && destinationHubId.isNotBlank()) {
                    val distanceKm = cleanDistance(routeColumns[3])
                    val typicalDelayMin = cleanDelay(routeColumns[4])
                    routes.add(RouteRaw(id, originHubId, destinationHubId, distanceKm, typicalDelayMin))
                }
            } else {
                println("Warning: route row $csvLineNumber was skipped because the number of columns is invalid.")
            }
        }
    }

    return routes
}
fun cleanId(id: String, fieldName: String, csvLineNumber: Int): String {
    val cleanedId = id.trim().uppercase()
    if (id.isBlank()) {
        println("Warning: route row $csvLineNumber was skipped because $fieldName is missing.")
    }
    return cleanedId

}
fun cleanDistance(distanceBeforeCleaning: String): Double {
    val distanceAfterCleaning = distanceBeforeCleaning.replace("km", "", ignoreCase = true).trim()
    val validatedDistance: Double
    if (
        distanceAfterCleaning.isBlank() ||
        distanceAfterCleaning.equals("N/A", ignoreCase = true) ||
        distanceAfterCleaning.equals("null", ignoreCase = true)
    ) {
        validatedDistance = -1.0
    } else {
        validatedDistance = distanceAfterCleaning.toDoubleOrNull() ?: -1.0
    }
    return validatedDistance

}
fun cleanDelay(delayBeforeCleaning: String): Int {
    val delayAfterCleaning = delayBeforeCleaning.trim()
    val validatedDelay: Int
    if (
        delayAfterCleaning.isBlank() ||
        delayAfterCleaning.equals("N/A", ignoreCase = true) ||
        delayAfterCleaning.equals("null", ignoreCase = true)
    ) {
        validatedDelay = -1
    } else {
        validatedDelay = delayAfterCleaning.toIntOrNull() ?: -1
    }
    return validatedDelay

}
fun main() {
    val routes = parseRoutes()

    for (route in routes) {
        println(route)
    }
}
package org.example.dataparsing

import org.example.data.dataholder.RouteRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

fun readRouteLines(): List<String> {
    val routeFilePath = Path("src/main/resources/routes.csv")
    val routeLines: List<String>
    if (routeFilePath.exists()) {
        routeLines = routeFilePath.readLines()
    } else {
        println("Warning: routes.csv was not found.")
        routeLines = emptyList()
    }
    return routeLines

}
fun parseRoutes(): List<RouteRaw> {
    val routes = mutableListOf<RouteRaw>()
    val routeLines = readRouteLines()
    val firstDataLineIndex = 1
    for (lineIndex in firstDataLineIndex until routeLines.size) {
        val currentRouteLine = routeLines[lineIndex]
        val csvLineNumber = lineIndex + 1
        processRouteLine(routes, currentRouteLine, csvLineNumber)
    }
    return routes
}

fun processRouteLine(routes: MutableList<RouteRaw>, currentRouteLine: String, csvLineNumber: Int
) {
    if (currentRouteLine.isNotBlank()) {
        val routeColumns = currentRouteLine.split(",").map { column -> column.trim() }
        validateAndAddRoute(routes, routeColumns, csvLineNumber)
    }

}
fun validateAndAddRoute(routes: MutableList<RouteRaw>, routeColumns: List<String>, csvLineNumber: Int) {
    val validColumnCount = hasValidRouteColumnCount(routeColumns)
    if (validColumnCount) {
        addRoute(routes, routeColumns, csvLineNumber)
    } else {
        println(
            "Warning: route row $csvLineNumber was skipped " +
                    "because the number of columns is invalid."
        )
    }

}
fun hasValidRouteColumnCount(routeColumns: List<String>): Boolean {
    val expectedColumnCount = 5
    val validColumnCount: Boolean
    if (routeColumns.size == expectedColumnCount) {
        validColumnCount = true
    } else {
        validColumnCount = false
    }
    return validColumnCount

}
fun addRoute(routes: MutableList<RouteRaw>, routeColumns: List<String>, csvLineNumber: Int) {
    val id = cleanId(routeColumns[0], "route ID", csvLineNumber)
    val originHubId = cleanId(routeColumns[1], "origin hub ID", csvLineNumber)
    val destinationHubId = cleanId(routeColumns[2], "destination hub ID", csvLineNumber)
    val routeIdsAreValid = areRouteIdsValid(id, originHubId, destinationHubId)
    if (routeIdsAreValid) {
        createAndAddRoute(routes, routeColumns, id, originHubId, destinationHubId)
    }

}
fun cleanId(idBeforeCleaning: String, fieldName: String, csvLineNumber: Int): String {
    val cleanedId = idBeforeCleaning.trim().uppercase()
    if (cleanedId.isBlank()) {
        println(
            "Warning: route row $csvLineNumber was skipped " +
                    "because $fieldName is missing."
        )
    }
    return cleanedId

}
fun areRouteIdsValid(id: String, originHubId: String, destinationHubId: String): Boolean {
    val routeIdsAreValid: Boolean
    if (id.isNotBlank() &&
        originHubId.isNotBlank() &&
        destinationHubId.isNotBlank()
    ) {
        routeIdsAreValid = true
    } else {
        routeIdsAreValid = false
    }
    return routeIdsAreValid

}
fun createAndAddRoute(routes: MutableList<RouteRaw>,
    routeColumns: List<String>, id: String,
    originHubId: String, destinationHubId: String) {
    val distanceKm = cleanDistance(routeColumns[3])
    val typicalDelayMin = cleanDelay(routeColumns[4])
    val route = RouteRaw(
        id,
        originHubId,
        destinationHubId,
        distanceKm,
        typicalDelayMin
    )
    routes.add(route)

}
fun cleanDistance(distanceBeforeCleaning: String): Double {
    val distanceAfterCleaning = distanceBeforeCleaning.replace("km", "", ignoreCase = true)
        .trim()
    val invalidDistance = -1.0
    val validatedDistance: Double
    if (
        distanceAfterCleaning.isBlank() ||
        distanceAfterCleaning.equals(
            "N/A", ignoreCase = true) ||
        distanceAfterCleaning.equals("null", ignoreCase = true)) {
        validatedDistance = invalidDistance
    } else {
        validatedDistance =
            distanceAfterCleaning.toDoubleOrNull() ?: invalidDistance
    }
    return validatedDistance

}
fun cleanDelay(delayBeforeCleaning: String): Int {
    val delayAfterCleaning = delayBeforeCleaning.trim()
    val invalidDelay = -1
    val validatedDelay: Int
    if (
        delayAfterCleaning.isBlank() ||
        delayAfterCleaning.equals("N/A", ignoreCase = true) ||
        delayAfterCleaning.equals("null", ignoreCase = true)) {
        validatedDelay = invalidDelay
    } else {
        validatedDelay = delayAfterCleaning.toIntOrNull() ?: invalidDelay
    }
    return validatedDelay

}


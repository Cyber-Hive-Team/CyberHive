package org.example.data.dataparsing

import org.example.data.dataholder.RouteRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

private const val HEADER_LINE_INDEX = 0
private const val EXPECTED_ROUTE_COLUMN_COUNT = 5
private const val INVALID_DISTANCE = -1.0
private const val INVALID_DELAY = -1

private const val ROUTE_ID_COLUMN = 0
private const val ORIGIN_HUB_COLUMN = 1
private const val DESTINATION_HUB_COLUMN = 2
private const val DISTANCE_COLUMN = 3
private const val DELAY_COLUMN = 4

fun parseRoutes(filePath: String): List<RouteRaw> {
    val routeLines = readRouteLines(filePath)
    val routes = mutableListOf<RouteRaw>()

    for (lineIndex in HEADER_LINE_INDEX + 1 until routeLines.size) {
        val currentRouteLine = routeLines[lineIndex]
        val csvLineNumber = lineIndex + 1

        processRouteLine(
            routes = routes,
            currentRouteLine = currentRouteLine,
            csvLineNumber = csvLineNumber
        )
    }

    return routes
}

private fun readRouteLines(filePath: String): List<String> {
    val routeFilePath = Path(filePath)

    if (!routeFilePath.exists()) {
        println("Warning: routes.csv was not found at $filePath.")
        return emptyList()
    }

    return routeFilePath.readLines()
}

private fun processRouteLine(
    routes: MutableList<RouteRaw>,
    currentRouteLine: String,
    csvLineNumber: Int
) {
    if (currentRouteLine.isBlank()) return

    val routeColumns = currentRouteLine
        .split(",")
        .map { it.trim() }

    validateAndAddRoute(
        routes = routes,
        routeColumns = routeColumns,
        csvLineNumber = csvLineNumber
    )
}

private fun validateAndAddRoute(
    routes: MutableList<RouteRaw>,
    routeColumns: List<String>,
    csvLineNumber: Int
) {
    if (routeColumns.size != EXPECTED_ROUTE_COLUMN_COUNT) {
        println(
            "Warning: route row $csvLineNumber was skipped " +
                    "because the number of columns is invalid."
        )
        return
    }

    addRoute(
        routes = routes,
        routeColumns = routeColumns,
        csvLineNumber = csvLineNumber
    )
}

private fun addRoute(
    routes: MutableList<RouteRaw>,
    routeColumns: List<String>,
    csvLineNumber: Int
) {
    val id = cleanId(
        routeColumns[ROUTE_ID_COLUMN],
        "route ID",
        csvLineNumber
    )

    val originHubId = cleanId(
        routeColumns[ORIGIN_HUB_COLUMN],
        "origin hub ID",
        csvLineNumber
    )

    val destinationHubId = cleanId(
        routeColumns[DESTINATION_HUB_COLUMN],
        "destination hub ID",
        csvLineNumber
    )

    if (!areRouteIdsValid(id, originHubId, destinationHubId)) {
        return
    }

    val distanceKm = cleanDistance(routeColumns[DISTANCE_COLUMN])
    val typicalDelayMin = cleanDelay(routeColumns[DELAY_COLUMN])

    routes.add(
        RouteRaw(
            id = id,
            originHubId = originHubId,
            destinationHubId = destinationHubId,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    )
}

private fun cleanId(
    idBeforeCleaning: String,
    fieldName: String,
    csvLineNumber: Int
): String {
    val cleanedId = idBeforeCleaning
        .trim()
        .uppercase()

    if (cleanedId.isBlank()) {
        println(
            "Warning: route row $csvLineNumber was skipped " +
                    "because $fieldName is missing."
        )
    }

    return cleanedId
}

private fun areRouteIdsValid(
    id: String,
    originHubId: String,
    destinationHubId: String
): Boolean {
    return id.isNotBlank() &&
            originHubId.isNotBlank() &&
            destinationHubId.isNotBlank()
}

private fun cleanDistance(distanceBeforeCleaning: String): Double {
    val cleanedDistance = distanceBeforeCleaning
        .replace("km", "", ignoreCase = true)
        .trim()

    if (
        cleanedDistance.isBlank() ||
        cleanedDistance.equals("N/A", ignoreCase = true) ||
        cleanedDistance.equals("null", ignoreCase = true)
    ) {
        return INVALID_DISTANCE
    }

    return cleanedDistance.toDoubleOrNull() ?: INVALID_DISTANCE
}

private fun cleanDelay(delayBeforeCleaning: String): Int {
    val cleanedDelay = delayBeforeCleaning.trim()

    if (
        cleanedDelay.isBlank() ||
        cleanedDelay.equals("N/A", ignoreCase = true) ||
        cleanedDelay.equals("null", ignoreCase = true)
    ) {
        return INVALID_DELAY
    }

    return cleanedDelay.toIntOrNull() ?: INVALID_DELAY
}
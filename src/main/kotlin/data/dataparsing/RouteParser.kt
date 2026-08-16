package org.example.data.dataparsing

import org.example.data.dataholder.RouteParseResult
import org.example.data.dataholder.RouteRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

private const val FIRST_DATA_ROW_INDEX = 1
private const val EXPECTED_COLUMN_COUNT = 5
private const val INVALID_DISTANCE = -1.0
private const val INVALID_DELAY = -1

fun parseRoutes(): RouteParseResult {
    val warnings = mutableListOf<String>()
    val routes = mutableListOf<RouteRaw>()
    val lines = readRouteLines(warnings)

    for (index in FIRST_DATA_ROW_INDEX until lines.size) {
        processRouteLine(
            routes = routes,
            line = lines[index],
            rowNumber = index + 1,
            warnings = warnings
        )
    }

    return RouteParseResult(
        routes = routes,
        warnings = warnings
    )
}

private fun readRouteLines(
    warnings: MutableList<String>
): List<String> {
    val path = Path("src/main/resources/routes.csv")

    if (!path.exists()) {
        warnings.add(
            "Warning: routes.csv was not found."
        )
        return emptyList()
    }

    return path.readLines()
}

private fun processRouteLine(
    routes: MutableList<RouteRaw>,
    line: String,
    rowNumber: Int,
    warnings: MutableList<String>
) {
    if (line.isBlank()) {
        return
    }

    val columns = line
        .split(",")
        .map { it.trim() }

    if (columns.size != EXPECTED_COLUMN_COUNT) {
        warnings.add(
            "Warning: route row $rowNumber was skipped " +
                    "because the number of columns is invalid."
        )
        return
    }

    val route = createRoute(
        columns = columns,
        rowNumber = rowNumber,
        warnings = warnings
    )

    if (route != null) {
        routes.add(route)
    }
}

private fun createRoute(
    columns: List<String>,
    rowNumber: Int,
    warnings: MutableList<String>
): RouteRaw? {
    val id = cleanId(
        value = columns[0],
        fieldName = "route ID",
        rowNumber = rowNumber,
        warnings = warnings
    )

    val originHubId = cleanId(
        value = columns[1],
        fieldName = "origin hub ID",
        rowNumber = rowNumber,
        warnings = warnings
    )

    val destinationHubId = cleanId(
        value = columns[2],
        fieldName = "destination hub ID",
        rowNumber = rowNumber,
        warnings = warnings
    )

    if (
        id.isBlank() ||
        originHubId.isBlank() ||
        destinationHubId.isBlank()
    ) {
        return null
    }

    return RouteRaw(
        id = id,
        originHubId = originHubId,
        destinationHubId = destinationHubId,
        distanceKm = cleanDistance(columns[3]),
        typicalDelayMin = cleanDelay(columns[4])
    )
}

private fun cleanId(
    value: String,
    fieldName: String,
    rowNumber: Int,
    warnings: MutableList<String>
): String {
    val cleaned = value.trim().uppercase()

    if (cleaned.isBlank()) {
        warnings.add(
            "Warning: route row $rowNumber was skipped " +
                    "because $fieldName is missing."
        )
    }

    return cleaned
}

private fun cleanDistance(value: String): Double {
    val cleaned = value
        .replace("km", "", ignoreCase = true)
        .trim()

    if (
        cleaned.isBlank() ||
        cleaned.equals("N/A", ignoreCase = true) ||
        cleaned.equals("null", ignoreCase = true)
    ) {
        return INVALID_DISTANCE
    }

    return cleaned.toDoubleOrNull() ?: INVALID_DISTANCE
}

private fun cleanDelay(value: String): Int {
    val cleaned = value.trim()

    if (
        cleaned.isBlank() ||
        cleaned.equals("N/A", ignoreCase = true) ||
        cleaned.equals("null", ignoreCase = true)
    ) {
        return INVALID_DELAY
    }

    return cleaned.toIntOrNull() ?: INVALID_DELAY
}
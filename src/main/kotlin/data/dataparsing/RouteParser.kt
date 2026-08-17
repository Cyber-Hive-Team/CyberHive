package org.example.data.dataparsing

import org.example.data.dataholder.RouteParseResult
import org.example.data.dataholder.RouteRaw
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readLines

private const val FIRST_DATA_ROW_INDEX = 1
private const val EXPECTED_COLUMN_COUNT = 5

private const val ID_INDEX = 0
private const val ORIGIN_INDEX = 1
private const val DESTINATION_INDEX = 2
private const val DISTANCE_INDEX = 3
private const val DELAY_INDEX = 4

private const val INVALID_DISTANCE = -1.0
private const val INVALID_DELAY = -1

fun parseRoutes(filePath: String): RouteParseResult {
    val warnings = mutableListOf<String>()
    val routes = mutableListOf<RouteRaw>()
    val lines = readRouteLines(filePath, warnings)

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
    filePath: String,
    warnings: MutableList<String>
): List<String> {
    val path = Path(filePath)

    if (!path.exists()) {
        warnings.add("Warning: routes.csv was not found.")
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

    createRoute(
        columns = columns,
        rowNumber = rowNumber,
        warnings = warnings
    )?.let(routes::add)
}

private fun createRoute(
    columns: List<String>,
    rowNumber: Int,
    warnings: MutableList<String>
): RouteRaw? {
    val id = cleanId(
        columns[ID_INDEX],
        "route ID",
        rowNumber,
        warnings
    )

    val origin = cleanId(
        columns[ORIGIN_INDEX],
        "origin hub ID",
        rowNumber,
        warnings
    )

    val destination = cleanId(
        columns[DESTINATION_INDEX],
        "destination hub ID",
        rowNumber,
        warnings
    )

    if (id.isBlank() || origin.isBlank() || destination.isBlank()) {
        return null
    }

    return RouteRaw(
        id = id,
        originHubId = origin,
        destinationHubId = destination,
        distanceKm = cleanDistance(columns[DISTANCE_INDEX]),
        typicalDelayMin = cleanDelay(columns[DELAY_INDEX])
    )
}

private fun cleanId(
    value: String,
    fieldName: String,
    rowNumber: Int,
    warnings: MutableList<String>
): String {
    val cleaned = value
        .trim()
        .uppercase()

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

    if (isInvalidValue(cleaned)) {
        return INVALID_DISTANCE
    }

    return cleaned.toDoubleOrNull() ?: INVALID_DISTANCE
}

private fun cleanDelay(value: String): Int {
    val cleaned = value.trim()

    if (isInvalidValue(cleaned)) {
        return INVALID_DELAY
    }

    return cleaned.toIntOrNull() ?: INVALID_DELAY
}

private fun isInvalidValue(value: String): Boolean {
    return value.isBlank() ||
            value.equals("N/A", ignoreCase = true) ||
            value.equals("null", ignoreCase = true)
}
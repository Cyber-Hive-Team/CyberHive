package org.example.data.dataparsing

import org.example.data.dataholder.RawResult
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

private data class RouteIds(
    val id: String,
    val origin: String,
    val destination: String
)

fun parseRoutes(filePath: String): List<RawResult<RouteRaw>> {

    val lines = readRouteLines(filePath)
    val rawRoutesResultList: List<RawResult<RouteRaw>> =
        lines
            .drop(FIRST_DATA_ROW_INDEX)
            .mapIndexed { index, line ->
                processRouteLine(
                    line = line,
                    rowNumber = index + FIRST_DATA_ROW_INDEX
                )
            }

    return rawRoutesResultList
}

private fun readRouteLines(
    filePath: String
): List<String> {
    val path = Path(filePath)

    if (!path.exists()) {
        return emptyList()
    }

    return path.readLines()
}

private fun processRouteLine(
    line: String,
    rowNumber: Int,
): RawResult<RouteRaw> {
    if (line.isBlank()) {
        return RawResult(
            rawData = null, errorMessage = "Route row $rowNumber is empty"
        )
    }
    val columns = line.split(",").map { it.trim() }
    if (columns.size != EXPECTED_COLUMN_COUNT) {
        return RawResult(
            rawData = null, errorMessage = "Route row $rowNumber was skipped because the number of columns is invalid"
        )
    }
    val route = createRoute(columns = columns)
    if (route == null) {
        return RawResult(rawData = null, errorMessage = "Route row $rowNumber has missing required fields")
    }
    return RawResult(rawData = route, errorMessage = null)
}


private fun createRoute(
    columns: List<String>
): RouteRaw? {
    val ids = extractRouteIds(columns)
        ?: return null

    return RouteRaw(
        id = ids.id,
        originHubId = ids.origin,
        destinationHubId = ids.destination,
        distanceKm = cleanDistance(columns[DISTANCE_INDEX]),
        typicalDelayMin = cleanDelay(columns[DELAY_INDEX])
    )
}

private fun extractRouteIds(
    columns: List<String>
): RouteIds? {
    val id = cleanId(
        columns[ID_INDEX]
    )
    val origin = cleanId(
        columns[ORIGIN_INDEX]
    )
    val destination = cleanId(
        columns[DESTINATION_INDEX]
    )

    if (id.isBlank() || origin.isBlank() || destination.isBlank()) {
        return null
    }

    return RouteIds(id, origin, destination)
}

private fun cleanId(
    value: String
): String {
    val cleaned = value.trim().uppercase()
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


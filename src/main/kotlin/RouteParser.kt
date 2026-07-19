package org.example

fun main() {
    val routes = parseRoutes()
    println("\nFinal routes:")
    for (route in routes) {
        println(
            "routeId=${route.id}, " +
                    "originHubId=${route.originHubId}, " +
                    "destinationHubId=${route.destinationHubId}, " +
                    "distanceKm=${route.distanceKm}, " +
                    "typicalDelayMin=${route.typicalDelayMin}"
        )
    }
}

fun parseRoutes(): List<RouteRaw> {
    val routes = mutableListOf<RouteRaw>()
    val lines = readRouteLines()

    for (index in 1 until lines.size) {
        val currentRouteLine = lines[index]
        val csvLineNumber = index + 1
        if (currentRouteLine.isBlank()) {
            continue
        }
        val routeColumns = currentRouteLine.split(",")

        val totalExpectedColumns = 5
        if (routeColumns.size != totalExpectedColumns) {
            println(
                "Warning: route row $csvLineNumber was skipped " +
                        "because the number of columns is invalid."
            )
            continue
        }


        val id = cleanId(routeColumns[0])
        val originHubId = cleanId(routeColumns[1])
        val destinationHubId = cleanId(routeColumns[2])

        if (id.isBlank()) {
            println(
                "Warning: route row $csvLineNumber was skipped " +
                        "because route ID is missing."
            )
            continue
        }

        if (originHubId.isBlank()) {
            println(
                "Warning: route row $csvLineNumber was skipped " +
                        "because origin hub ID is missing."
            )
            continue
        }

        if (destinationHubId.isBlank()) {
            println(
                "Warning: route row $csvLineNumber was skipped " +
                        "because destination hub ID is missing."
            )
            continue
        }

        val distanceKm = cleanDistance(routeColumns[3])
        val typicalDelayMin = cleanDelay(routeColumns[4])

        val route = RouteRaw(
            id = id,
            originHubId = originHubId,
            destinationHubId = destinationHubId,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )

        routes.add(route)
    }

    return routes
}

fun cleanId(idBeforeCleaning: String): String {
    return idBeforeCleaning.trim().uppercase()
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

    return  distanceAfterCleaning.toDoubleOrNull() ?: -1.0
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
fun readRouteLines(): List<String> {
    val inputStream =
        object {}.javaClass.getResourceAsStream("/routes.csv")

    if (inputStream == null) {
        println("Warning: routes.csv was not found.")
        return emptyList()
    }
    return inputStream.bufferedReader().use { reader -> reader.readLines() }
}
package org.example.domain.algorithm.search

import org.example.domain.model.RoutingResult
import org.example.domain.model.Warehouse

private const val INITIAL_DISTANCE = 0.0

class BreadthFirstSearchRouter(
    private val graph: WarehouseGraph
) : Router {
    override fun findPath(
        start: Warehouse,
        destination: Warehouse
    ): RoutingResult {

        if (start == destination) {
            return RoutingResult(
                path = listOf(start),
                distanceKm = INITIAL_DISTANCE
            )
        }

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<Warehouse>()
        val parent = mutableMapOf<Warehouse, Warehouse?>()

        queue.addLast(start)
        visited.add(start)
        parent[start] = null

        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()

            if (currentWarehouse == destination) {
                val path = buildPath(
                    destination,
                    parent
                )

                return RoutingResult(
                    path = path,
                    distanceKm = calculatePathDistance(path)
                )
            }

            for (neighbor in graph.getNeighbors(currentWarehouse)) {
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    parent[neighbor] = currentWarehouse
                    queue.addLast(neighbor)
                }
            }
        }

        return RoutingResult(
            path = emptyList(),
            distanceKm = Double.POSITIVE_INFINITY
        )
    }


    private fun buildPath(destination: Warehouse, parent: Map<Warehouse, Warehouse?>): List<Warehouse> {
        val path = mutableListOf<Warehouse>()
        var currentWarehouse: Warehouse? = destination
        while (currentWarehouse != null) {
            path.add(currentWarehouse)
            currentWarehouse = parent[currentWarehouse]
        }
        path.reverse()
        return path
    }
    private fun calculatePathDistance(
        path: List<Warehouse>
    ): Double {

        if (path.size < 2) {
            return INITIAL_DISTANCE
        }

        var totalDistance = INITIAL_DISTANCE

        for (index in 0 until path.lastIndex) {

            val currentWarehouse =
                path[index]

            val nextWarehouse =
                path[index + 1]

            val route =
                currentWarehouse
                    .getOutgoingRoutes()
                    .firstOrNull {
                        it.destinationWarehouse ==
                                nextWarehouse
                    }
                    ?: nextWarehouse
                        .getOutgoingRoutes()
                        .firstOrNull {
                            it.destinationWarehouse ==
                                    currentWarehouse
                        }

            if (route != null) {
                totalDistance += route.distanceKm
            }
        }

        return totalDistance
    }
}

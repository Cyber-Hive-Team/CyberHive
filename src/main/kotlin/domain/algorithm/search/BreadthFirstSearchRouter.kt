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
            val current = queue.removeFirst()

            if (current == destination) {
                val path = buildPath(
                    destination,
                    parent
                )

                return RoutingResult(
                    path = path,
                    distanceKm = calculatePathDistance(path)
                )
            }

            for (neighbor in graph.getNeighbors(current)) {
                if (neighbor !in visited) {
                    visited.add(neighbor)
                    parent[neighbor] = current
                    queue.addLast(neighbor)
                }
            }
        }

        return RoutingResult(
            path = emptyList(),
            distanceKm = Double.POSITIVE_INFINITY
        )
    }

    private fun buildPath(
        destination: Warehouse,
        parent: Map<Warehouse, Warehouse?>
    ): List<Warehouse> {

        val path = mutableListOf<Warehouse>()
        var current: Warehouse? = destination

        while (current != null) {
            path.add(current)
            current = parent[current]
        }

        path.reverse()
        return path
    }

    private fun calculatePathDistance(
        path: List<Warehouse>
    ): Double =
        path.zipWithNext().sumOf { (current, next) ->
            findDistance(current, next)
        }

    private fun findDistance(
        current: Warehouse,
        next: Warehouse
    ): Double {

        val route = current.getOutgoingRoutes()
            .firstOrNull {
                it.destinationWarehouse == next
            }

        if (route != null) {
            return route.distanceKm
        }

        return next.getOutgoingRoutes()
            .firstOrNull {
                it.destinationWarehouse == current
            }
            ?.distanceKm
            ?: INITIAL_DISTANCE
    }
}
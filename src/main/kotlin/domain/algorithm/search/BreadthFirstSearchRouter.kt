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
            return createResult(listOf(start))
        }

        val search = initializeSearch(start)

        while (search.queue.isNotEmpty()) {
            val current = search.queue.removeFirst()

            if (current == destination) {
                return createResult(
                    buildPath(destination, search.parent)
                )
            }

            addNeighbors(current, search)
        }

        return RoutingResult(
            path = emptyList(),
            distanceKm = Double.POSITIVE_INFINITY
        )
    }

    private fun initializeSearch(
        start: Warehouse
    ): SearchState {
        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<Warehouse>()
        val parent = mutableMapOf<Warehouse, Warehouse?>()

        queue.addLast(start)
        visited.add(start)
        parent[start] = null

        return SearchState(queue, visited, parent)
    }

    private fun addNeighbors(
        current: Warehouse,
        search: SearchState
    ) {
        for (neighbor in graph.getNeighbors(current)) {
            if (neighbor in search.visited) continue

            search.visited.add(neighbor)
            search.parent[neighbor] = current
            search.queue.addLast(neighbor)
        }
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

    private fun createResult(
        path: List<Warehouse>
    ): RoutingResult =
        RoutingResult(
            path = path,
            distanceKm = calculatePathDistance(path)
        )

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
        val outgoing = current.getOutgoingRoutes()
            .firstOrNull {
                it.destinationWarehouse == next
            }

        if (outgoing != null) {
            return outgoing.distanceKm
        }

        return next.getOutgoingRoutes()
            .firstOrNull {
                it.destinationWarehouse == current
            }
            ?.distanceKm
            ?: INITIAL_DISTANCE
    }
}

private data class SearchState(
    val queue: ArrayDeque<Warehouse>,
    val visited: MutableSet<Warehouse>,
    val parent: MutableMap<Warehouse, Warehouse?>
)
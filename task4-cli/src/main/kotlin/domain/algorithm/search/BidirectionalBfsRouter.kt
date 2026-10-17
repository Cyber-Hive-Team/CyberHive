package org.example.domain.algorithm.search

import org.example.domain.model.result.RoutingResult
import org.example.domain.model.Warehouse
import java.util.*

private const val INITIAL_DISTANCE = 0.0

class BidirectionalBfsRouter(
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

        val forward = initializeSearch(start)
        val backward = initializeSearch(destination)
        val path = searchPath(forward, backward)

        return RoutingResult(
            path = path,
            distanceKm = calculatePathDistance(path)
        )
    }

    private fun initializeSearch(
        warehouse: Warehouse
    ): SearchSide {

        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<Warehouse>()
        val parents = mutableMapOf<Warehouse, Warehouse?>()

        queue.addLast(warehouse)
        visited.add(warehouse)
        parents[warehouse] = null

        return SearchSide(queue, visited, parents)
    }

    private fun searchPath(
        forward: SearchSide,
        backward: SearchSide
    ): List<Warehouse> {

        while (
            forward.queue.isNotEmpty() &&
            backward.queue.isNotEmpty()
        ) {
            val meeting = search(forward, backward.visited)

            if (meeting != null) {
                return reconstructPath(
                    meeting,
                    forward.parents,
                    backward.parents
                )
            }

            val reverseMeeting = search(
                backward,
                forward.visited
            )

            if (reverseMeeting != null) {
                return reconstructPath(
                    reverseMeeting,
                    forward.parents,
                    backward.parents
                )
            }
        }

        return emptyList()
    }

    private fun search(
        side: SearchSide,
        oppositeVisited: Set<Warehouse>
    ): Warehouse? {

        val current = side.queue.removeFirst()

        for (neighbor in graph.getNeighbors(current)) {
            if (neighbor in side.visited) {
                continue
            }

            side.visited.add(neighbor)
            side.parents[neighbor] = current

            if (neighbor in oppositeVisited) {
                return neighbor
            }

            side.queue.addLast(neighbor)
        }

        return null
    }

    private fun calculatePathDistance(
        path: List<Warehouse>
    ): Double {

        return path.zipWithNext().sumOf { (current, next) ->
            findDistance(current, next)
        }
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

private fun reconstructPath(
    meetingPoint: Warehouse,
    forwardParents: Map<Warehouse, Warehouse?>,
    backwardParents: Map<Warehouse, Warehouse?>
): List<Warehouse> {

    val path = mutableListOf<Warehouse>()
    var current: Warehouse? = meetingPoint

    while (current != null) {
        path.add(current)
        current = forwardParents[current]
    }

    path.reverse()
    current = backwardParents[meetingPoint]

    while (current != null) {
        path.add(current)
        current = backwardParents[current]
    }

    return path
}

private data class SearchSide(
    val queue: ArrayDeque<Warehouse>,
    val visited: MutableSet<Warehouse>,
    val parents: MutableMap<Warehouse, Warehouse?>
)

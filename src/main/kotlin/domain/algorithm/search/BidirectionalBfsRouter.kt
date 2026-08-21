package org.example.domain.algorithm.search

import org.example.domain.model.RoutingResult
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

        val forwardVisited = mutableSetOf<Warehouse>()
        val backwardVisited = mutableSetOf<Warehouse>()

        initializeSearch(
            start,
            forwardQueue,
            forwardVisited
        )

        initializeSearch(
            destination,
            backwardQueue,
            backwardVisited
        )

        val path = searchPath(
            forwardQueue,
            backwardQueue,
            forwardVisited,
            backwardVisited
        )

        if (path.isEmpty()) {
            return RoutingResult(
                path = emptyList(),
                distanceKm = Double.POSITIVE_INFINITY
            )
        }

        return RoutingResult(
            path = path,
            distanceKm = calculatePathDistance(path)
        )
        return searchPath(forward, backward)
    }

    private fun initializeSearch(
        warehouse: Warehouse
    ): SearchSide {
        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<Warehouse>()
        val parents = mutableMapOf<Warehouse, Warehouse?>()

        queue.add(warehouse)
        visited.add(warehouse)
        parents[warehouse] = null

        return SearchSide(
            queue = queue,
            visited = visited,
            parents = parents
        )
    }

    private fun searchPath(
        forwardQueue: ArrayDeque<Warehouse>,
        backwardQueue: ArrayDeque<Warehouse>,
        forwardVisited: MutableSet<Warehouse>,
        backwardVisited: MutableSet<Warehouse>
    ): List<Warehouse> {

        val forwardParents =
            mutableMapOf<Warehouse, Warehouse?>()

        val backwardParents =
            mutableMapOf<Warehouse, Warehouse?>()

        forwardParents[forwardQueue.first()] = null
        backwardParents[backwardQueue.first()] = null

        while (
            forwardQueue.isNotEmpty() &&
            backwardQueue.isNotEmpty()
        ) {

            val meetingPoint = search(
                forwardQueue,
                forwardVisited,
                backwardVisited,
                forwardParents
            )

            if (meetingPoint != null) {
                return reconstructPath(
                    meetingPoint,
                    forwardParents,
                    backwardParents
    private fun searchPath(forward: SearchSide, backward: SearchSide): List<Warehouse> {
        while (forward.queue.isNotEmpty() && backward.queue.isNotEmpty()) {
            val meetingPoint = search(forward, backward.visited)
            if (meetingPoint != null) {
                return reconstructPath(
                    meetingPoint,
                    forward.parents,
                    backward.parents
                )
            }

            val backwardMeetingPoint = search(
                backwardQueue,
                backwardVisited,
                forwardVisited,
                backwardParents
                backward,
                forward.visited
            )

            if (backwardMeetingPoint != null) {
                return reconstructPath(
                    backwardMeetingPoint,
                    forwardParents,
                    backwardParents
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

        val current = queue.removeFirst()

        for (neighbor in graph.getNeighbors(current)) {

            if (neighbor in visited) {
                continue
            }

            visited.add(neighbor)
            parents[neighbor] = current

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
}

private data class SearchSide(
    val queue: ArrayDeque<Warehouse>,
    val visited: MutableSet<Warehouse>,
    val parents: MutableMap<Warehouse, Warehouse?>
)

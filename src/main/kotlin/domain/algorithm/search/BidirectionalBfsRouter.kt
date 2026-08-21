package org.example.domain.algorithm.search

import org.example.domain.model.Warehouse
import java.util.*

class BidirectionalBfsRouter(
    private val graph: WarehouseGraph
) : Router {

    override fun findPath(
        start: Warehouse,
        destination: Warehouse
    ): List<Warehouse> {

        if (start == destination) {
            return listOf(start)
        }

        val forward = initializeSearch(start)
        val backward = initializeSearch(destination)

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
                backward,
                forward.visited
            )
            if (backwardMeetingPoint != null) {
                return reconstructPath(
                    backwardMeetingPoint,
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

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

        val forwardQueue = ArrayDeque<Warehouse>()
        val backwardQueue = ArrayDeque<Warehouse>()

        val forwardVisited = mutableSetOf<Warehouse>()
        val backwardVisited = mutableSetOf<Warehouse>()

        initializeSearch(start, forwardQueue, forwardVisited)

        initializeSearch(destination, backwardQueue, backwardVisited)
        return searchPath(
            forwardQueue, backwardQueue, forwardVisited, backwardVisited
        )
    }

    private fun initializeSearch(
        warehouse: Warehouse,
        queue: ArrayDeque<Warehouse>,
        visited: MutableSet<Warehouse>
    ) {
        queue.add(warehouse)
        visited.add(warehouse)
    }

    private fun searchPath(
        forwardQueue: ArrayDeque<Warehouse>, backwardQueue: ArrayDeque<Warehouse>,
        forwardVisited: MutableSet<Warehouse>, backwardVisited: MutableSet<Warehouse>,
    ): List<Warehouse> {
        val forwardParents = mutableMapOf<Warehouse, Warehouse?>()
        val backwardParents = mutableMapOf<Warehouse, Warehouse?>()
        forwardParents[forwardQueue.first()] = null
        backwardParents[backwardQueue.first()] = null
        while (forwardQueue.isNotEmpty() && backwardQueue.isNotEmpty()) {
            val meetingPoint = search(
                forwardQueue, forwardVisited,
                backwardVisited, forwardParents
            )
            if (meetingPoint != null) {
                return reconstructPath(
                    meetingPoint,
                    forwardParents, backwardParents
                )
            }
            val backwardMeetingPoint = search(
                backwardQueue, backwardVisited,
                forwardVisited, backwardParents
            )
            if (backwardMeetingPoint != null) {
                return reconstructPath(
                    backwardMeetingPoint,
                    forwardParents, backwardParents
                )
            }
        }
        return emptyList()
    }

    private fun search(
        queue: ArrayDeque<Warehouse>,
        visited: MutableSet<Warehouse>,
        oppositeVisited: Set<Warehouse>,
        parents: MutableMap<Warehouse, Warehouse?>
    ): Warehouse? {
        val current = queue.removeFirst()
        for (neighbor in graph.getNeighbors(current)) {
            if (neighbor in visited) {
                continue
            }
            visited.add(neighbor)
            parents[neighbor] = current
            if (neighbor in oppositeVisited) {
                return neighbor
            }

            queue.addLast(neighbor)
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
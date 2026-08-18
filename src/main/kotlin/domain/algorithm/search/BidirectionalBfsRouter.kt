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

        return searchPath(
            forwardQueue,
            backwardQueue,
            forwardVisited,
            backwardVisited
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
        forwardQueue: ArrayDeque<Warehouse>,
        backwardQueue: ArrayDeque<Warehouse>,
        forwardVisited: MutableSet<Warehouse>,
        backwardVisited: MutableSet<Warehouse>
    ): List<Warehouse> {

        while (
            forwardQueue.isNotEmpty() &&
            backwardQueue.isNotEmpty()
        ) {
            val meetingPoint = search(
                forwardQueue,
                forwardVisited,
                backwardVisited
            )

            if (meetingPoint != null) {
                return listOf(meetingPoint)
            }

            val backwardMeetingPoint = search(
                backwardQueue,
                backwardVisited,
                forwardVisited
            )

            if (backwardMeetingPoint != null) {
                return listOf(backwardMeetingPoint)
            }
        }

        return emptyList()
    }

    private fun search(
        queue: ArrayDeque<Warehouse>,
        visited: MutableSet<Warehouse>,
        oppositeVisited: Set<Warehouse>
    ): Warehouse? {

        val current = queue.removeFirst()

        for (neighbor in graph.getNeighbors(current)) {
            if (neighbor in visited) {
                continue
            }

            visited.add(neighbor)

            if (neighbor in oppositeVisited) {
                return neighbor
            }

            queue.addLast(neighbor)
        }

        return null
    }
}
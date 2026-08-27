package org.example.domain.algorithm.search

import org.example.domain.model.RoutingResult
import org.example.domain.model.Warehouse

private const val INITIAL_DISTANCE = 0.0

class DijkstraRouter(
    private val allWarehouses: List<Warehouse>
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

        val distances = initializeDistances(start)
        val previous = mutableMapOf<Warehouse, Warehouse>()
        val processed = mutableSetOf<Warehouse>()

        exploreNetwork(
            destination,
            distances,
            previous,
            processed
        )

        val path = buildPath(
            start,
            destination,
            previous,
            distances
        )

        return RoutingResult(
            path = path,
            distanceKm =
                distances[destination]
                    ?: Double.POSITIVE_INFINITY
        )
    }

    private fun initializeDistances(
        start: Warehouse
    ): MutableMap<Warehouse, Double> {

        val distances = mutableMapOf<Warehouse, Double>()

        for (warehouse in allWarehouses) {
            distances[warehouse] =
                Double.POSITIVE_INFINITY
        }

        distances[start] = INITIAL_DISTANCE
        return distances
    }

    private fun exploreNetwork(
        destination: Warehouse,
        distances: MutableMap<Warehouse, Double>,
        previous: MutableMap<Warehouse, Warehouse>,
        processed: MutableSet<Warehouse>
    ) {

        var current =
            findClosestUnprocessed(
                distances,
                processed
            )

        while (
            current != null &&
            current != destination
        ) {
            updateNeighbors(
                current,
                distances,
                previous,
                processed
            )

            processed.add(current)

            current =
                findClosestUnprocessed(
                    distances,
                    processed
                )
        }
    }

    private fun findClosestUnprocessed(
        distances: Map<Warehouse, Double>,
        processed: Set<Warehouse>
    ): Warehouse? {

        var closest: Warehouse? = null
        var shortest = Double.POSITIVE_INFINITY

        for ((warehouse, distance) in distances) {
            if (
                warehouse !in processed &&
                distance < shortest
            ) {
                closest = warehouse
                shortest = distance
            }
        }

        return closest
    }

    private fun updateNeighbors(
        current: Warehouse,
        distances: MutableMap<Warehouse, Double>,
        previous: MutableMap<Warehouse, Warehouse>,
        processed: Set<Warehouse>
    ) {

        val currentDistance =
            distances[current] ?: return

        for (route in current.getOutgoingRoutes()) {
            val neighbor = route.destinationWarehouse

            if (neighbor in processed) {
                continue
            }

            val newDistance =
                currentDistance + route.distanceKm

            val oldDistance =
                distances[neighbor]
                    ?: Double.POSITIVE_INFINITY

            if (newDistance < oldDistance) {
                distances[neighbor] = newDistance
                previous[neighbor] = current
            }
        }
    }

    private fun buildPath(
        start: Warehouse,
        destination: Warehouse,
        previous: Map<Warehouse, Warehouse>,
        distances: Map<Warehouse, Double>
    ): List<Warehouse> {

        if (
            distances[destination] ==
            Double.POSITIVE_INFINITY
        ) {
            return emptyList()
        }

        val path = mutableListOf<Warehouse>()
        var current: Warehouse? = destination

        while (current != null) {
            path.add(current)

            if (current == start) {
                break
            }

            current = previous[current]
        }

        if (path.lastOrNull() != start) {
            return emptyList()
        }

        path.reverse()
        return path
    }
}
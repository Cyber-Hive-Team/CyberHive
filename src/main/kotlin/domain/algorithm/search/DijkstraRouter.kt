package org.example.domain.algorithm.search

import org.example.domain.model.Warehouse

private const val INITIAL_DISTANCE = 0.0

class DijkstraRouter(
    private val allWarehouses: List<Warehouse>
) : Router {

    override fun findPath(
        start: Warehouse,
        destination: Warehouse
    ): List<Warehouse> {

        if (start == destination) {
            return listOf(start)
        }

        val distances = initializeDistances(start)
        val previousWarehouses = mutableMapOf<Warehouse, Warehouse>()
        val processedWarehouses = mutableSetOf<Warehouse>()

        while (processedWarehouses.size < allWarehouses.size) {

            val currentWarehouse = findClosestUnprocessedWarehouse(
                distances,
                processedWarehouses
            ) ?: break

            if (currentWarehouse == destination) {
                break
            }

            updateNeighborDistances(
                currentWarehouse,
                distances,
                previousWarehouses,
                processedWarehouses
            )

            processedWarehouses.add(currentWarehouse)
        }

        return buildPath(
            start,
            destination,
            previousWarehouses,
            distances
        )
    }

    private fun initializeDistances(
        start: Warehouse
    ): MutableMap<Warehouse, Double> {

        val distances = mutableMapOf<Warehouse, Double>()

        for (warehouse in allWarehouses) {
            distances[warehouse] = Double.POSITIVE_INFINITY
        }

        distances[start] = INITIAL_DISTANCE

        return distances
    }

    private fun findClosestUnprocessedWarehouse(
        distances: Map<Warehouse, Double>,
        processedWarehouses: Set<Warehouse>
    ): Warehouse? {

        var closestWarehouse: Warehouse? = null
        var shortestDistance = Double.POSITIVE_INFINITY

        for ((warehouse, distance) in distances) {

            if (
                warehouse !in processedWarehouses &&
                distance < shortestDistance
            ) {
                closestWarehouse = warehouse
                shortestDistance = distance
            }
        }

        return closestWarehouse
    }

    private fun updateNeighborDistances(
        currentWarehouse: Warehouse,
        distances: MutableMap<Warehouse, Double>,
        previousWarehouses: MutableMap<Warehouse, Warehouse>,
        processedWarehouses: Set<Warehouse>
    ) {
        val currentDistance = distances[currentWarehouse]
            ?: return

        for (route in currentWarehouse.getOutgoingRoutes()) {

            val neighbor = route.destinationWarehouse

            if (neighbor in processedWarehouses) {
                continue
            }

            val newDistance = currentDistance + route.distanceKm
            val oldDistance =
                distances[neighbor] ?: Double.POSITIVE_INFINITY

            if (newDistance < oldDistance) {
                distances[neighbor] = newDistance
                previousWarehouses[neighbor] = currentWarehouse
            }
        }
    }

    private fun buildPath(
        start: Warehouse,
        destination: Warehouse,
        previousWarehouses: Map<Warehouse, Warehouse>,
        distances: Map<Warehouse, Double>
    ): List<Warehouse> {

        if (distances[destination] == Double.POSITIVE_INFINITY) {
            return emptyList()
        }

        val path = mutableListOf<Warehouse>()
        var currentWarehouse: Warehouse? = destination

        while (currentWarehouse != null) {
            path.add(currentWarehouse)

            if (currentWarehouse == start) {
                break
            }

            currentWarehouse = previousWarehouses[currentWarehouse]
        }

        if (path.lastOrNull() != start) {
            return emptyList()
        }

        path.reverse()
        return path
    }
}
package org.example.domain.algorithm.search

import domain.algorithm.search.Router
import org.example.domain.model.Warehouse

class BreadthFirstSearchRouter : Router {

    override fun findPath(start: Warehouse, destination: Warehouse): List<Warehouse> {
        val queue = ArrayDeque<Warehouse>()
        val visited = mutableSetOf<Warehouse>()
        val parent = mutableMapOf<Warehouse, Warehouse?>()
        queue.addLast(start)
        visited.add(start)
        parent[start] = null
        while (queue.isNotEmpty()) {
            val currentWarehouse = queue.removeFirst()
            if (currentWarehouse == destination) {
                return buildPath(destination = destination, parent = parent)
            }
            val neighbors = getNeighbors(currentWarehouse)
            for (neighbor in neighbors) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor)
                    parent[neighbor] = currentWarehouse
                    queue.addLast(neighbor)
                }
            }
        }
        return emptyList()
    }

    private fun getNeighbors(warehouse: Warehouse): List<Warehouse> {
        val neighbors = mutableListOf<Warehouse>()
        for (route in warehouse.getOutgoingRoutes()) {
            neighbors.add(route.destinationWarehouse)
        }
        return neighbors
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
}
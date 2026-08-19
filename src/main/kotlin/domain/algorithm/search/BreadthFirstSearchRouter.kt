package org.example.domain.algorithm.search

import org.example.domain.model.Warehouse

class BreadthFirstSearchRouter(
    private val graph: WarehouseGraph
) : Router {

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
            val neighbors = graph.getNeighbors(currentWarehouse)
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
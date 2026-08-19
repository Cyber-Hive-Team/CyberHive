package org.example.domain.algorithm.search

import org.example.domain.model.Warehouse

class EvaluatingWarehouseGraph(
    private val graph: WarehouseGraph
) : WarehouseGraph {

    private val evaluatedWarehouses = mutableSetOf<Warehouse>()

    override fun getNeighbors(
        warehouse: Warehouse
    ): List<Warehouse> {

        evaluatedWarehouses.add(warehouse)

        return graph.getNeighbors(warehouse)
    }

    fun getEvaluatedCount(): Int {
        return evaluatedWarehouses.size
    }
}
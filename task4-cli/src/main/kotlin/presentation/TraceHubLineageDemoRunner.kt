package org.example.presentation

import org.example.domain.algorithm.tree.WarehouseHierarchyBuilder
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseNode
import org.example.domain.usecase.TraceHubLineageUseCase

class TraceHubLineageDemoRunner(
    private val warehouses: List<Warehouse>,
    private val routes: List<Route>,
    private val traceHubLineageUseCaseFactory:
        (WarehouseNode) -> TraceHubLineageUseCase
) {

    fun run(warehouseId: String) {

        val tree = WarehouseHierarchyBuilder(warehouses = warehouses, routes = routes).build()

        if (tree == null) {
            println("Could not build warehouse hierarchy.")
            return
        }

        val traceHubLineageUseCase = traceHubLineageUseCaseFactory(tree)
        val lineage = traceHubLineageUseCase(warehouseId = warehouseId)

        if (lineage.isEmpty()) {
            println("Warehouse was not found.")
            return
        }
        println("\n=== Warehouse Lineage ===")
        lineage.forEach { node -> println("${node.warehouse.name} [${node.level}]") }
    }
}

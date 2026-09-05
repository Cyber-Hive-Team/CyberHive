package org.example.presentation

import org.example.domain.algorithm.tree.WarehouseHierarchyBuilder
import org.example.domain.usecase.TraceHubLineageUseCase

class TraceHubLineageDemoRunner {

    fun run(warehouseId: String) {
        val data = DataLoader().load()
        val tree = WarehouseHierarchyBuilder(warehouses = data.warehouses, routes = data.routes).build()
        if (tree == null) {
            println("Could not build warehouse hierarchy.")
            return
        }
        val traceHubLineageUseCase = TraceHubLineageUseCase(tree = tree)
        val lineage = traceHubLineageUseCase(warehouseId = warehouseId)
        if (lineage.isEmpty()) {
            println("Warehouse was not found.")
            return
        }
        println("\n=== Warehouse Lineage ===")
        lineage.forEach { node ->
            println(
                "${node.warehouse.name} [${node.level}]"
            )
        }

    }
}

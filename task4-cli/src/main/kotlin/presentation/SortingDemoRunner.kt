package org.example.presentation

import org.example.domain.model.Warehouse

class SortingDemoRunner(
    private val warehouses: List<Warehouse>
) {

    fun run() {
        println("\n=== Quick Sort (Weight Descending) ===")

        val warehouse = warehouses.firstOrNull()
            ?: return println("No hub available.")

        printCargo("Before Sorting", warehouse)

        warehouses
            .filter { it.getCargoQueue().isNotEmpty() }
            .forEach { it.sortCargoQueue() }

        printCargo("After Sorting", warehouse)
    }

    private fun printCargo(title: String, warehouse: Warehouse) {
        println("--- $title (${warehouse.id}) ---")

        warehouse.getCargoQueue().forEach {
            println(
                "${it.id} " +
                        "(Priority: ${it.priority}, " +
                        "Weight: ${it.weight}kg)"
            )
        }
    }
}

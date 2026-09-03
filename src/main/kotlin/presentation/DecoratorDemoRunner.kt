package org.example.presentation

import org.example.domain.decorator.ColdChainDecorator
import org.example.domain.decorator.ExpressInsuranceDecorator
import org.example.domain.decorator.FragileHandlingDecorator
import org.example.domain.model.PackageComponent
import org.example.domain.model.Warehouse

class DecoratorDemoRunner(
    private val warehouses: List<Warehouse>
) {

    fun run() {
        println("\n=== Decorator Pattern ===")

        val item = warehouses
            .firstOrNull()
            ?.getCargoQueue()
            ?.firstOrNull()
            ?: return println("No package available.")

        var decorated: PackageComponent = item
        printDecorator("Original", decorated)

        decorated = FragileHandlingDecorator(decorated)
        printDecorator("After Fragile Handling", decorated)

        decorated = ColdChainDecorator(decorated)
        printDecorator("After Cold Chain", decorated)

        decorated = ExpressInsuranceDecorator(decorated)
        printDecorator("After Express Insurance", decorated)
    }

    private fun printDecorator(title: String, item: PackageComponent) {
        println("\n$title:")
        println("Description: ${item.getDescription()}")
        println("Transit Rate: ${item.calculateTransitRate()}")
    }
}

package org.example.presentation

import org.example.domain.model.Warehouse
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.ExpressStrategy
import org.example.domain.pricing.FragileStrategy
import org.example.domain.pricing.RoutePricingEngine

class PricingDemoRunner(
    private val warehouses: List<Warehouse>
) {

    fun run() {
        println("\n=== Strategy Pattern Pricing ===")

        val warehouse = warehouses.firstOrNull()
        val item = warehouse?.getCargoQueue()?.firstOrNull()
        val route = warehouse?.getOutgoingRoutes()?.firstOrNull()

        if (item == null || route == null) {
            println("No package or route available.")
            return
        }

        val engine = RoutePricingEngine(EcoStrategy())

        listOf(
            "EcoStrategy" to EcoStrategy(),
            "ExpressStrategy" to ExpressStrategy(),
            "FragileStrategy" to FragileStrategy()
        ).forEach { (name, strategy) ->
            engine.setStrategy(strategy)

            println("$name price: $" + engine.calculatePrice(item, route))
        }
    }
}

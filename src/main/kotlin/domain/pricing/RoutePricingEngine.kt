package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Route

class RoutePricingEngine(private var strategy: DispatchStrategy) {

    fun setStrategy(newStrategy: DispatchStrategy) {
        this.strategy = newStrategy
    }

    fun calculatePrice(cargoPackage: Package, route: Route): Double {
        val transitCost = strategy.calculateTransitCost(cargoPackage, route)
        val priorityMultiplier = strategy.getPriorityMultiplier(cargoPackage.priority)
        return transitCost * priorityMultiplier
    }
}

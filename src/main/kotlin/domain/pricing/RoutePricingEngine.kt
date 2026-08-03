package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Route

class RoutePricingEngine(private var strategy: DispatchStrategy) {

    fun setStrategy(newStrategy: DispatchStrategy) {
        this.strategy = newStrategy
    }

    fun calculatePrice(pkg: Package, route: Route): Double {
        val transitCost = strategy.calculateTransitCost(pkg, route)
        val priorityMultiplier = strategy.getPriorityMultiplier(pkg.priority)
        return transitCost * priorityMultiplier
    }
}

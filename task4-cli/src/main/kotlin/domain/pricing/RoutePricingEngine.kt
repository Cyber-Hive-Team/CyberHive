package org.example.domain.pricing

import org.example.domain.model.Package

class RoutePricingEngine(private var strategy: DispatchStrategy) {

    fun setStrategy(newStrategy: DispatchStrategy) {
        this.strategy = newStrategy
    }

    fun calculatePrice(cargoPackage: Package, distanceKm: Double): Double {
        val transitCost = strategy.calculateTransitCost(cargoPackage, distanceKm)
        val priorityMultiplier = strategy.getPriorityMultiplier(cargoPackage.priority)
        return transitCost * priorityMultiplier
    }
}

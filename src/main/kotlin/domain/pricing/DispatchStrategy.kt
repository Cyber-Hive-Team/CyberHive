package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority

interface DispatchStrategy {
    fun calculateTransitCost(
        cargoPackage: Package, distanceKm: Double
    ): Double
    fun getPriorityMultiplier(priority: Priority): Double
}

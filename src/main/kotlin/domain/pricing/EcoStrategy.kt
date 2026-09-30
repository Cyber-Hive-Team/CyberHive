package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority

class EcoStrategy : DispatchStrategy {

    private val weightMultiplier = 0.8
    private val distanceMultiplier = 0.3
    private val priorityMultiplier = 2.0

    override fun calculateTransitCost(
        cargoPackage: Package, distanceKm: Double
    ): Double {

        return (cargoPackage.weight * weightMultiplier) +
                (distanceKm * distanceMultiplier)
    }
    override fun getPriorityMultiplier(priority: Priority): Double = priorityMultiplier
}

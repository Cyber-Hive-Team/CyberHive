package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority

class FragileStrategy : DispatchStrategy {

    private val weightMultiplier = 1.2
    private val distanceMultiplier = 0.4
    private val safetyFee = 25.0
    private val priorityMultiplier = 1.3

    override fun calculateTransitCost(cargoPackage: Package, distanceKm: Double): Double {
        return (cargoPackage.weight * weightMultiplier) +
                (distanceKm * distanceMultiplier) +
               safetyFee
    }
    override fun getPriorityMultiplier(priority: Priority): Double = priorityMultiplier
}

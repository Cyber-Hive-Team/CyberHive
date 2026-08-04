package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Route

class EcoStrategy : DispatchStrategy {

    private val weightMultiplier = 0.8
    private val distanceMultiplier = 0.3
    private val priorityMultiplier = 2.0

    override fun calculateTransitCost(cargoPackage: Package, route: Route): Double {

        return (cargoPackage.weight * weightMultiplier) +
                (route.distanceKm * distanceMultiplier)
    }
    override fun getPriorityMultiplier(priority: Priority): Double = priorityMultiplier
}
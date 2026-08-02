package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Route

class ExpressStrategy : DispatchStrategy {
    private val WEIGHT_MULTIPLIER = 1.5
    private val DISTANCE_MULTIPLIER = 0.8
    private val PRIORITY_MULTIPLIER = 2.0

    override fun calculateTransitCost(pkg: Package, route: Route): Double {
        return (pkg.weight * WEIGHT_MULTIPLIER) + (route.distanceKm * DISTANCE_MULTIPLIER)
    }

    override fun getPriorityMultiplier(priority: Priority): Double = PRIORITY_MULTIPLIER
}
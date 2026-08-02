package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Route

class ExpressStrategy : DispatchStrategy {
    private val weightMultiplier = 1.5
    private val distanceMultiplier = 0.8
    private val priorityMultiplier = 2.0
    override fun calculateTransitCost(pkg: Package, route: Route): Double {
        return (pkg.weight * weightMultiplier) + (route.distanceKm * distanceMultiplier)
    }

    override fun getPriorityMultiplier(priority: Priority): Double = priorityMultiplier
}
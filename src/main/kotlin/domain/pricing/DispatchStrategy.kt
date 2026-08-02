package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Route

interface DispatchStrategy {
    fun calculateTransitCost(pkg: Package, distanceKm: Route): Double
    fun getPriorityMultiplier(priority: Priority): Double
}

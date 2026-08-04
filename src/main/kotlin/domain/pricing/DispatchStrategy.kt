package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Route

interface DispatchStrategy {
    fun calculateTransitCost(cargoPackage: Package, route: Route): Double
    fun getPriorityMultiplier(priority: Priority): Double
}

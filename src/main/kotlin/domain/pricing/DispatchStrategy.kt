package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.data.dataholder.Priority

interface DispatchStrategy {
    fun calculateTransitCost(pkg: Package, distanceKm: Double): Double
    fun getPriorityMultiplier(priority: Priority): Double
}

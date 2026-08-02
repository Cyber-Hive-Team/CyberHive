package org.example.domain.pricing

import org.example.domain.model.Package
import org.example.data.dataholder.Priority
import org.example.domain.model.Route


class ExpressStrategy : DispatchStrategy {
    override fun calculateTransitCost(pkg: Package, distanceKm: Route): Double {
        return (pkg.weight * 1.5) + (distanceKm.distanceKm * 0.8)
    }
    override fun getPriorityMultiplier(priority: Priority): Double =2.0

}
package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

class CalculatePackageDeliveryCostUseCase {

    operator fun invoke(
        packageItem: Package,
        vehicle: Vehicle,
        distanceKm: Double
    ): Double {

        val baseWeightCost = packageItem.weight * 0.5
        val distanceCost = distanceKm * vehicle.costPerKm
        val priorityMultiplier = when (packageItem.priority.toString().uppercase()) {
            "EXPRESS" -> 1.5
            "URGENT" -> 1.25
            else -> 1.0
        }

        return (baseWeightCost + distanceCost) * priorityMultiplier
    }
}
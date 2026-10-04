package org.example.domain.dispatch

import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException

class VehicleCapacityReservations {

    private val reservedWeights = mutableMapOf<String, Double>()

    fun reserve(vehicle: Vehicle, cargoWeight: Double) {

        if (!vehicle.maxCapacityKg.isFinite() ||
            vehicle.maxCapacityKg <= 0.0
        ) {
            throw EntityValidationException(
                "Vehicle capacity must be positive and finite"
            )
        }

        if (!cargoWeight.isFinite() || cargoWeight <= 0.0) {
            throw EntityValidationException(
                "Cargo weight must be positive and finite"
            )
        }

        val alreadyReserved = reservedWeights[vehicle.id] ?: 0.0
        val newTotal = alreadyReserved + cargoWeight

        if (!newTotal.isFinite() || newTotal > vehicle.maxCapacityKg) {
            throw EntityValidationException(
                "Not enough capacity in vehicle ${vehicle.id}"
            )
        }

        reservedWeights[vehicle.id] = newTotal
    }

    fun reservedWeightFor(vehicleId: String): Double {
        return reservedWeights[vehicleId] ?: 0.0
    }
}

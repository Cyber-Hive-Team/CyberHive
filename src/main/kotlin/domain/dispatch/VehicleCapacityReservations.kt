package org.example.domain.dispatch

import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException

class VehicleCapacityReservations {

    private val reservedWeights = mutableMapOf<String, Double>()

    fun reserve(vehicle: Vehicle, cargoWeight: Double) {
        validateVehicleCapacity(vehicle)
        validateCargoWeight(cargoWeight)
        val newTotal = reservedWeightFor(vehicle.id) + cargoWeight
        validateAvailableCapacity(vehicle = vehicle, newTotal = newTotal)
        reservedWeights[vehicle.id] = newTotal
    }

    fun reservedWeightFor(vehicleId: String): Double {
        return reservedWeights[vehicleId] ?: NO_RESERVED_WEIGHT_KG
    }

    private fun validateVehicleCapacity(vehicle: Vehicle) {
        if (!vehicle.maxCapacityKg.isFinite() || vehicle.maxCapacityKg <= ZERO_WEIGHT_KG) {
            throw EntityValidationException("Vehicle capacity must be positive and finite")
        }
    }

    private fun validateCargoWeight(cargoWeight: Double) {
        if (!cargoWeight.isFinite() || cargoWeight <= ZERO_WEIGHT_KG) {
            throw EntityValidationException("Cargo weight must be positive and finite")
        }
    }

    private fun validateAvailableCapacity(vehicle: Vehicle, newTotal: Double) {
        if (!newTotal.isFinite() || newTotal > vehicle.maxCapacityKg) {
            throw EntityValidationException("Not enough capacity in vehicle ${vehicle.id}")
        }
    }

    companion object {
        private const val ZERO_WEIGHT_KG = 0.0
        private const val NO_RESERVED_WEIGHT_KG = 0.0
    }
}

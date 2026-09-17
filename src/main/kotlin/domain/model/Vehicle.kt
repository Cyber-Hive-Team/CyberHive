package org.example.domain.model

import org.example.domain.model.exception.InvalidVehicleIdException

private const val VEHICLE_ID_PREFIX = "^TRK-\\d{4}$"

data class Vehicle(
    val id: String,
    val maxCapacityKg: Double,
    val costPerKm: Double,
    val currentHub: Warehouse
){

    init {
        validateId()
    }

    private fun validateId() {
        if (!id.matches(Regex(VEHICLE_ID_PREFIX))) {
            throw InvalidVehicleIdException()
        }
    }
}

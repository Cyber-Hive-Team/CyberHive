package org.example.domain.model.input

import org.example.domain.model.Warehouse

data class UpdateVehicleInput(
    val id: String,
    val maxCapacityKg: Double? = null,
    val costPerKm: Double? = null,
    val currentHub: Warehouse? = null
)

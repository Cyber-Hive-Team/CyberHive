package org.example.domain.repository

import org.example.domain.model.Vehicle

data class VehicleRepositoryResult(
    val vehicles: List<Vehicle>,
    val warnings: List<String>
)
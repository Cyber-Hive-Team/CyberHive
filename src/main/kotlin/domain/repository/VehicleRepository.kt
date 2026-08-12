package org.example.domain.repository

import org.example.domain.model.Vehicle

interface VehicleRepository {
    fun getAllVehicles(): List<Vehicle>
}
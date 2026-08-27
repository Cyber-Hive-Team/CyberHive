package org.example.domain.repository

import org.example.domain.model.Result
import org.example.domain.model.Vehicle

interface VehicleRepository {
    fun getVehicles(): Result<List<Vehicle>>
}


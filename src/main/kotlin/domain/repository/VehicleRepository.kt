package org.example.domain.repository

import org.example.domain.model.Result
import org.example.domain.model.Vehicle

interface VehicleRepository {
    fun getVehicles(): Result<List<Vehicle>>
    fun getVehicleById(vehicleId: String): Vehicle?
    fun getVehiclesByWarehouseId(warehouseId: String): Result<List<Vehicle>>
    fun reassignVehicle(vehicleId: String, warehouseId: String): Boolean
}


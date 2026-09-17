package org.example.domain.repository

import org.example.domain.model.Vehicle
import org.example.domain.model.result.Result

interface VehicleRepository {
    fun getVehicles(): Result<List<Vehicle>>
    fun getVehicleById(vehicleId: String): Vehicle?
    fun getVehiclesByWarehouseId(warehouseId: String): Result<List<Vehicle>>
    fun reassignVehicle(vehicleId: String, warehouseId: String): Boolean
    fun removeVehicle(vehicleId: String): Boolean
    suspend fun getRemoteById(
        vehicleId: String
    ): Vehicle?

    suspend fun save(
        vehicle: Vehicle
    ): Vehicle

    suspend fun update(
        vehicle: Vehicle
    ): Vehicle

    suspend fun delete(
        id: String
    ): Boolean
}



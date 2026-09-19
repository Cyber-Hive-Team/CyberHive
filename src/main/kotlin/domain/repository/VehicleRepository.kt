package org.example.domain.repository

import org.example.domain.model.Vehicle

interface VehicleRepository {
    suspend fun getVehicles(): Result<List<Vehicle>>
    suspend fun getVehiclesByWarehouseId(warehouseId: String): Result<List<Vehicle>>
    suspend fun reassignVehicle(vehicleId: String, warehouseId: String): Boolean
    suspend fun removeVehicle(vehicleId: String): Boolean
    suspend fun getById(
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



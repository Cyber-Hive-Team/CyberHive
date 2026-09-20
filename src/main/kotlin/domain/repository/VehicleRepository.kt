package org.example.domain.repository

import org.example.domain.model.Vehicle


interface VehicleRepository {
    suspend fun getVehicles(): Result<List<Vehicle>>
    suspend fun getVehiclesByWarehouseId(warehouseId: String): Result<List<Vehicle>>
    suspend fun reassignVehicle(vehicleId: String, warehouseId: String): Result<Boolean>
    suspend fun removeVehicle(vehicleId: String): Result<Boolean>
    suspend fun getById(
        vehicleId: String
    ): Result<Vehicle>
    suspend fun save(
        vehicle: Vehicle
    ): Result<Vehicle>

    suspend fun update(
        vehicle: Vehicle
    ): Result<Vehicle>

    suspend fun delete(
        id: String
    ): Result<Boolean>
}



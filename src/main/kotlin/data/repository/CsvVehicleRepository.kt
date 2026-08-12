package org.example.data.repository

import org.example.data.dataparsing.parseFleet
import org.example.domain.model.Vehicle
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

class CsvVehicleRepository(
    private val filePath: String,
    private val warehouseRepository: WarehouseRepository
) : VehicleRepository {

    override fun getAllVehicles(): List<Vehicle> {
        val rawVehicles = parseFleet(filePath)
        val warehouseMap = warehouseRepository.getAllWarehouses().associateBy { it.id }

        return rawVehicles.mapNotNull { raw ->
            val currentWarehouse = warehouseMap[raw.currentHubId]

            if (currentWarehouse == null) {
                println("Warning: Vehicle ${raw.id} skipped - warehouse not found.")
                return@mapNotNull null
            }

            Vehicle(
                id = raw.id,
                maxCapacityKg = raw.maxCapacityKg,
                costPerKm = raw.costPerKm,
                currentHub = currentWarehouse
            )
        }
    }
}
package org.example.domain.usecase

import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

class AddVehicleToHubUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(
        vehicleId: String,
        warehouseId: String
    ): Boolean {
        val vehicle = vehicleRepository.getVehicleById(vehicleId)
            ?: return false

        val warehouse = warehouseRepository.getWarehouseById(warehouseId)
            ?: return false

        warehouse.addVehicles(listOf(vehicle))

        return true
    }
}
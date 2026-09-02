package org.example.domain.usecase

import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.model.input.AddVehicleToHubInput

class AddVehicleToHubUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(
        input: AddVehicleToHubInput
    ): Boolean {
        val vehicle = vehicleRepository.getVehicleById(input.vehicleId)
            ?: return false

        val warehouse = warehouseRepository.getWarehouseById(input.warehouseId)
            ?: return false

        warehouse.addVehicles(listOf(vehicle))

        return true
    }
}

package org.example.domain.usecase

import org.example.domain.model.input.AddVehicleToHubInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository

class AddVehicleToHubUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(
        input: AddVehicleToHubInput
    ): Boolean {

        return vehicleRepository.getVehicleById(input.vehicleId)
            ?.let { vehicle ->
                warehouseRepository.getWarehouseById(input.warehouseId)
                    ?.let { warehouse ->
                        warehouse.addVehicles(listOf(vehicle))
                        true
                    }
            }
            ?: false
    }

}

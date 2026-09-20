package org.example.domain.usecase

import org.example.domain.model.Vehicle

import org.example.domain.model.input.AddVehicleToHubInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository


class AddVehicleToHubUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        input: AddVehicleToHubInput
    ): Result<Vehicle> {

        return runCatching {
            val vehicle =
                vehicleRepository
                    .getById(input.vehicleId)
                    .getOrThrow()

            val warehouse =
                warehouseRepository
                    .getById(input.warehouseId)
                    .getOrThrow()

            warehouse.addVehicles(
                listOf(vehicle)
            )

            vehicle
        }
    }
}



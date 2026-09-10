package org.example.domain.usecase

import org.example.domain.model.input.AddVehicleToHubInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.result.Result
import org.example.domain.model.Vehicle


class AddVehicleToHubUseCase(
    private val vehicleRepository: VehicleRepository,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(
        input: AddVehicleToHubInput
    ): Result<Vehicle> {

        val vehicle = vehicleRepository.getVehicleById(input.vehicleId)
            ?: throw VehicleNotFoundException()

        val warehouse = warehouseRepository.getWarehouseById(input.warehouseId)
            ?: throw WarehouseNotFoundException()

        warehouse.addVehicles(listOf(vehicle))

        return Result(vehicle)
    }
}



package org.example.domain.command

import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.AddVehicleToHubUseCase

class AddVehicleToHubCommand(
    private val vehicleId: String,
    private val warehouseId: String,
    private val addVehicleToHubUseCase: AddVehicleToHubUseCase,
    private val vehicleRepository: VehicleRepository
) : Command {

    private var addedVehicle = false

    override fun execute(): Boolean {
        addedVehicle = addVehicleToHubUseCase(vehicleId, warehouseId)
        return addedVehicle
    }

    override fun undo(): Boolean {
        if (!addedVehicle) return false
        return vehicleRepository.removeVehicle(vehicleId)
    }

}

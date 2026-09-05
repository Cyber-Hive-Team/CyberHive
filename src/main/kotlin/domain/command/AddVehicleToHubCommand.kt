package org.example.domain.command

import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.AddVehicleToHubUseCase
import org.example.domain.model.input.AddVehicleToHubInput
import org.example.domain.model.exception.CommandExecutionException

class AddVehicleToHubCommand(
    private val vehicleId: String,
    private val warehouseId: String,
    private val addVehicleToHubUseCase: AddVehicleToHubUseCase,
    private val vehicleRepository: VehicleRepository
) : Command {

    private var addedVehicle = false

    override fun execute(): Boolean {
        addedVehicle = addVehicleToHubUseCase(AddVehicleToHubInput(vehicleId, warehouseId))
        if (!addedVehicle) {
            throw CommandExecutionException("Failed to add vehicle '$vehicleId' to warehouse '$warehouseId'.")
        }

        return true
    }

    override fun undo(): Boolean {
        if (!addedVehicle) {
            throw CommandExecutionException("Cannot undo: Vehicle '$vehicleId' was not successfully added prior to undo.")

        }

        val removed = vehicleRepository.removeVehicle(vehicleId)
        if (!removed) {
            throw CommandExecutionException("Failed to remove vehicle '$vehicleId' during undo execution.")
        }

        addedVehicle = false
        return true
    }

}

package org.example.domain.command

import org.example.domain.model.exception.CommandExecutionException
import org.example.domain.model.input.AddVehicleToHubInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.AddVehicleToHubUseCase

class AddVehicleToHubCommand(
    private val vehicleId: String,
    private val warehouseId: String,
    private val addVehicleToHubUseCase: AddVehicleToHubUseCase,
    private val vehicleRepository: VehicleRepository
) : Command {

    private var addedVehicle = false

    override suspend fun execute(): Boolean {
        addVehicleToHubUseCase(
            AddVehicleToHubInput(vehicleId, warehouseId)
        )

        addedVehicle = true
        return true
    }

    override suspend fun undo(): Boolean {
        if (!addedVehicle) {
            throw CommandExecutionException(
                "Cannot undo: Vehicle '$vehicleId' was not successfully added prior to undo."
            )
        }

        val removed = vehicleRepository.removeVehicle(vehicleId)

        if (!removed) {
            throw CommandExecutionException(
                "Failed to remove vehicle '$vehicleId' during undo execution."
            )
        }

        addedVehicle = false
        return true
    }

    override suspend fun describe(): String {
        val currentHub = vehicleRepository.getById(vehicleId)?.currentHub?.id

        return "Add vehicle $vehicleId -> warehouse $warehouseId " +
                "| currently at: $currentHub"
    }
}

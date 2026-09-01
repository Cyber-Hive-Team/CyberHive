package org.example.domain.command

import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.AddVehicleToHubUseCase

class AddVehicleToHubCommand(
    private val vehicleId: String,
    private val warehouseId: String,
    private val addVehicleToHubUseCase: AddVehicleToHubUseCase,
    private val vehicleRepository: VehicleRepository
) : Command {

    private var AddedVehicle = false

    override fun execute(): Boolean {
        AddedVehicle = addVehicleToHubUseCase(vehicleId, warehouseId)
        return AddedVehicle
    }

    override fun undo(): Boolean {
        if (!AddedVehicle) return false
        return vehicleRepository.removeVehicle(vehicleId)
    }

}

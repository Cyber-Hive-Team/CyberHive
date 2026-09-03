package org.example.domain.command

import org.example.domain.model.result.VehicleTransferResult
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.RedistributeFleetUseCase

class RedistributeFleetCommand(
    private val redistributeFleetUseCase: RedistributeFleetUseCase,
    private val vehicleRepository: VehicleRepository
) : Command {
    private var transfers: List<VehicleTransferResult> = emptyList()

    override fun execute(): Boolean {
        transfers = redistributeFleetUseCase()
        return transfers.isNotEmpty()

    }

    override fun undo(): Boolean {
        if (transfers.isEmpty()) return false
        var success = true
        transfers.forEach { transfer ->
            val undone = vehicleRepository.reassignVehicle(
                vehicleId = transfer.vehicleId,
                warehouseId = transfer.fromWarehouseId
            )
            if (!undone) {
                success = false
            }
        }
        return success
    }

}


package org.example.domain.command

import org.example.domain.model.result.VehicleTransferResult
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.RedistributeFleetUseCase
import org.example.domain.model.exception.CommandExecutionException

class RedistributeFleetCommand(
    private val redistributeFleetUseCase: RedistributeFleetUseCase,
    private val vehicleRepository: VehicleRepository
) : Command {
    private var transfers: List<VehicleTransferResult> = emptyList()

    override fun execute(): Boolean {
        transfers = redistributeFleetUseCase()

        if (transfers.isEmpty()) {
            throw CommandExecutionException("Fleet redistribution resulted in no transfers.")
        }

        return true

    }

    override fun undo(): Boolean {
        if (transfers.isEmpty()) {
            throw CommandExecutionException("Cannot undo: No fleet transfers were executed to revert.")
        }

        transfers.forEach { transfer ->
            val undone = vehicleRepository.reassignVehicle(
                vehicleId = transfer.vehicleId,
                warehouseId = transfer.fromWarehouseId
            )
            if (!undone) {
                throw CommandExecutionException("Failed to revert vehicle '${transfer.vehicleId}' to warehouse '${transfer.fromWarehouseId}'.")
            }
        }

        transfers = emptyList()
        return true
    }
    override fun describe(): String {
        if (transfers.isEmpty()) return "Redistribute fleet: no transfers performed"
        val details = transfers.joinToString {
            "vehicle ${it.vehicleId} ${it.fromWarehouseId} -> ${it.toWarehouseId} (${it.capacityKg}kg)"
        }
        return "Redistribute fleet: ${transfers.size} transfer(s) [$details]"
    }

}


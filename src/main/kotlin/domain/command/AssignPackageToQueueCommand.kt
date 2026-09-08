package org.example.domain.command

import org.example.domain.model.Package
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.AssignPackageToCargoQueueUseCase
import org.example.domain.model.exception.CommandExecutionException

class AssignPackageToQueueCommand(
    private val warehouseId: String,
    private val cargoPackage: Package,
    private val assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase,
    private val warehouseRepository: WarehouseRepository
) : Command {

    private var addedPackage = false

    override fun execute(): Boolean {
        addedPackage = assignPackageToCargoQueueUseCase(warehouseId, cargoPackage)
        if (!addedPackage) {
            throw CommandExecutionException(
                "Failed to assign package '${cargoPackage.id}' to cargo queue in warehouse '$warehouseId'."
            )
        }
        return true
    }

    override fun undo(): Boolean {
        if (!addedPackage) {
            throw CommandExecutionException(
                "Cannot undo: Package '${cargoPackage.id}' was not assigned to queue prior to undo."
            )
        }

        val warehouse = warehouseRepository.getWarehouseById(warehouseId)
            ?: throw CommandExecutionException(
                "Failed to undo: Warehouse '$warehouseId' not found."
            )

        val removed = warehouse.removePackageFromCargoQueue(cargoPackage.id)
        if (!removed) {
            throw CommandExecutionException(
                "Failed to remove package '${cargoPackage.id}' from cargo queue in warehouse '$warehouseId'."
            )
        }

        addedPackage = false
        return true

    }

    override fun describe(): String {
        val queueIds = warehouseRepository.getWarehouseById(warehouseId)
            ?.getCargoQueue()
            ?.joinToString { it.id }
            .orEmpty()

        return "Assign package ${cargoPackage.id} -> warehouse $warehouseId " +
                "| queue now: [$queueIds]"
    }
}

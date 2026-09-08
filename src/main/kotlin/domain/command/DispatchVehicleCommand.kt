package org.example.domain.command

import org.example.domain.model.Package
import org.example.domain.usecase.AssignPackageToCargoQueueUseCase
import org.example.domain.usecase.DispatchVehicleUseCase

class DispatchVehicleCommand(
    private val vehicleId: String,
    private val dispatchVehicleUseCase: DispatchVehicleUseCase,
    private val assignPackageToCargoQueueUseCase: AssignPackageToCargoQueueUseCase
) : Command {

    private var dispatchedPackages: List<Package> = emptyList()

    override fun execute(): Boolean {
        dispatchedPackages = dispatchVehicleUseCase(vehicleId)

        if (dispatchedPackages.isEmpty()) {
            throw CommandExecutionException("Failed to dispatch vehicle '$vehicleId': No packages were dispatched.")
        }

        return true
    }

    override fun undo(): Boolean {
        if (dispatchedPackages.isEmpty()){
            throw CommandExecutionException("Cannot undo: No dispatched packages found to restore for vehicle '$vehicleId'.")
        }

        var isAllRestored = true
        dispatchedPackages.forEach { cargoPackage ->
            val restored = assignPackageToCargoQueueUseCase(
                warehouseId = cargoPackage.originWarehouse.id,
                cargoPackage = cargoPackage
            )
            if (!restored) {
                throw CommandExecutionException(
                    "Failed to restore package '${cargoPackage.id}' to origin warehouse '${cargoPackage.originWarehouse.id}' during undo."
                )
            }
        }
        dispatchedPackages = emptyList()
        return true

    }

    override fun describe(): String {
        val packageIds = dispatchedPackages.joinToString { it.id }

        return "Dispatch vehicle $vehicleId | loaded packages: [$packageIds]"
    }
}

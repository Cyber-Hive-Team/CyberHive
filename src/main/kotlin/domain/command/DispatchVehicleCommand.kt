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
        return dispatchedPackages.isNotEmpty()
    }

    override fun undo(): Boolean {
        if (dispatchedPackages.isEmpty()) return false

        var isAllRestored = true
        dispatchedPackages.forEach { cargoPackage ->
            val restored = assignPackageToCargoQueueUseCase(
                warehouseId = cargoPackage.originWarehouse.id,
                cargoPackage = cargoPackage
            )
            if (!restored) isAllRestored = false
        }
        return isAllRestored
    }
    override fun describe(): String {
        val packageIds = dispatchedPackages.joinToString { it.id }

        return "Dispatch vehicle $vehicleId | loaded packages: [$packageIds]"
    }
}

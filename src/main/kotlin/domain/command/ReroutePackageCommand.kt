package org.example.domain.command

import org.example.domain.usecase.ReroutePackageUseCase

class ReroutePackageCommand(
    private val packageId: String,
    private val oldDestinationWarehouseId: String,
    private val newDestinationWarehouseId: String,
    private val reroutePackageUseCase: ReroutePackageUseCase
) : Command {

    private var reroutedPackage = false

    override fun execute(): Boolean {
        val result = reroutePackageUseCase(packageId, newDestinationWarehouseId)
        reroutedPackage = result != null
        return reroutedPackage
    }

    override fun undo(): Boolean {
        if (!reroutedPackage) return false

        val reverseResult = reroutePackageUseCase(packageId, oldDestinationWarehouseId)
        return reverseResult != null
    }
}

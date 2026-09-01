package org.example.domain.command

import org.example.domain.usecase.ReroutePackageUseCase

class ReroutePackageCommand(
    private val packageId: String,
    private val oldDestinationWarehouseId: String,
    private val newDestinationWarehouseId: String,
    private val reroutePackageUseCase: ReroutePackageUseCase
) : Command {

    private var ReroutedPackage = false

    override fun execute(): Boolean {
        val result = reroutePackageUseCase(packageId, newDestinationWarehouseId)
        ReroutedPackage = result != null
        return ReroutedPackage
    }

    override fun undo(): Boolean {
        if (!ReroutedPackage) return false

        val reverseResult = reroutePackageUseCase(packageId, oldDestinationWarehouseId)
        return reverseResult != null
    }
}

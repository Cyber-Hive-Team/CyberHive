package org.example.domain.command

import org.example.domain.usecase.ReroutePackageUseCase
import org.example.domain.model.input.ReroutePackageInput

class ReroutePackageCommand(
    private val packageId: String,
    private val oldDestinationWarehouseId: String,
    private val newDestinationWarehouseId: String,
    private val reroutePackageUseCase: ReroutePackageUseCase
) : Command {

    private var reroutedPackage = false

    override fun execute(): Boolean {
        val input = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = newDestinationWarehouseId
        )

        val result = reroutePackageUseCase(input)
        reroutedPackage = result != null
        return reroutedPackage
    }

    override fun undo(): Boolean {
        val input = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = oldDestinationWarehouseId
        )
        if (!reroutedPackage) return false

        val reverseResult = reroutePackageUseCase(input)
        return reverseResult != null
    }
}

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

        return try {
            reroutePackageUseCase(input)
            reroutedPackage = true
            true
        } catch (e: Exception) {
            reroutedPackage = false
            false
        }

    }

    override fun undo(): Boolean {
        if (!reroutedPackage) return false

        val undoInput = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = oldDestinationWarehouseId
        )

        return try {
            reroutePackageUseCase(undoInput)
            true
        } catch (e: Exception) {
            false
        }
    }
}

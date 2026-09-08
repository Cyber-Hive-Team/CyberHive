package org.example.domain.command

import org.example.domain.usecase.ReroutePackageUseCase
import org.example.domain.model.input.ReroutePackageInput
import org.example.domain.model.exception.CommandExecutionException


class ReroutePackageCommand(
    private val packageId: String,
    private val oldDestinationWarehouseId: String,
    private val newDestinationWarehouseId: String,
    private val reroutePackageUseCase: ReroutePackageUseCase
) : Command {

    private var reroutedPackageSuccufully = false

    override fun execute(): Boolean {
        val input = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = newDestinationWarehouseId
        )

        reroutePackageUseCase(input)
        reroutedPackagesuccufully = true
        return true
        val result = reroutePackageUseCase(input)
        reroutedPackageSuccufully = result != null
        return reroutedPackageSuccufully
    }

    override fun undo(): Boolean {
        if (!reroutedPackagesuccufully) {
            throw CommandExecutionException("Cannot undo: Reroute package command was not executed successfully prior to undo.")
        }

        val reverseResult = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = oldDestinationWarehouseId
        )
        reroutePackageUseCase(reverseResult)
        reroutedPackagesuccufully = false
        return true
        if (!reroutedPackageSuccufully) return false

        val reverseResult = reroutePackageUseCase(input)
        return reverseResult != null
    }
    override fun describe(): String =
        "Reroute package $packageId | $oldDestinationWarehouseId -> $newDestinationWarehouseId"
}

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

    private var isReroutedSuccessfully = false

    override fun execute(): Boolean {
        val input = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = newDestinationWarehouseId
        )

        reroutePackageUseCase(input)
        isReroutedSuccessfully = true
        return true
    }

    override fun undo(): Boolean {
        if (!isReroutedSuccessfully) {
            throw CommandExecutionException(
                "Cannot undo: Reroute package command was not executed successfully prior to undo."
            )
        }

        val reverseInput = ReroutePackageInput(
            packageId = packageId,
            newDestinationWarehouseId = oldDestinationWarehouseId
        )

        reroutePackageUseCase(reverseInput)
        isReroutedSuccessfully = false
        return true
    }

    override fun describe(): String =
        "Reroute package $packageId | $oldDestinationWarehouseId -> $newDestinationWarehouseId"
}

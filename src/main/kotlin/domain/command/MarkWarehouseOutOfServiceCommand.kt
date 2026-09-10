package org.example.domain.command

import org.example.domain.model.WarehouseStatus
import org.example.domain.model.exception.CommandExecutionException
import org.example.domain.repository.WarehouseStatusRepository
import org.example.domain.usecase.MarkWarehouseOutOfServiceUseCase

class MarkWarehouseOutOfServiceCommand(
    private val warehouseId: String,
    private val markWarehouseOutOfServiceUseCase: MarkWarehouseOutOfServiceUseCase,
    private val warehouseStatusRepository: WarehouseStatusRepository
) : Command {

    private var previousStatus: WarehouseStatus? = null
    private var updated = false

    override fun execute(): Boolean {
        previousStatus = warehouseStatusRepository.getStatus(warehouseId)

        markWarehouseOutOfServiceUseCase(warehouseId)

        updated = true
        return true
    }

    override fun undo(): Boolean {
        if (!updated) {
            throw CommandExecutionException(
                "Cannot undo: Command was not executed successfully prior to undo."
            )
        }

        val statusToRestore = previousStatus
            ?: throw CommandExecutionException(
                "Cannot undo: Previous status for warehouse '$warehouseId' is missing."
            )

        val restored = warehouseStatusRepository.updateStatus(
            warehouseId = warehouseId,
            status = statusToRestore
        )

        if (!restored) {
            throw CommandExecutionException(
                "Failed to restore previous status for warehouse '$warehouseId'."
            )
        }

        updated = false
        return true
    }

    override fun describe(): String {
        val previous = previousStatus?.let { " (was $it)" } ?: ""
        return "Mark warehouse $warehouseId out of service$previous"
    }
}

package org.example.domain.command

import org.example.domain.model.WarehouseStatus
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
        updated = markWarehouseOutOfServiceUseCase(warehouseId)
        return updated

    }

    override fun undo(): Boolean {
        if (!updated || previousStatus == null) return false
        return warehouseStatusRepository.updateStatus(
            warehouseId = warehouseId,
            status = previousStatus!!
        )
    }
    override fun describe(): String {
        val previous = previousStatus?.let { " (was $it)" } ?: ""
        return "Mark warehouse $warehouseId out of service$previous"
    }

}


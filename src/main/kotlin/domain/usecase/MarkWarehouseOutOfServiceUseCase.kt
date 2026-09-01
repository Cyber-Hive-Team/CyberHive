package org.example.domain.usecase

import org.example.domain.model.WarehouseStatus
import org.example.domain.repository.WarehouseRepository
import org.example.domain.repository.WarehouseStatusRepository

class MarkWarehouseOutOfServiceUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseStatusRepository: WarehouseStatusRepository
) {

    operator fun invoke(warehouseId: String): Boolean {
        val warehouse = warehouseRepository.getWarehouseById(warehouseId)
            ?: return false

        return warehouseStatusRepository.updateStatus(
            warehouseId = warehouse.id,
            status = WarehouseStatus.OUT_OF_SERVICE
        )
    }
}

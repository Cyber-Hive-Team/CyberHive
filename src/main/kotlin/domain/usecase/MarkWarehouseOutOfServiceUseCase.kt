package org.example.domain.usecase

import org.example.domain.model.WarehouseStatus
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.result.WarehouseStatusResult
import org.example.domain.repository.WarehouseRepository
import org.example.domain.repository.WarehouseStatusRepository

class MarkWarehouseOutOfServiceUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseStatusRepository: WarehouseStatusRepository
) {

    operator fun invoke(warehouseId: String): WarehouseStatusResult {

        val warehouse = warehouseRepository.getWarehouseById(warehouseId)
            ?: throw WarehouseNotFoundException()

        warehouseStatusRepository.updateStatus(
            warehouseId = warehouse.id,
            status = WarehouseStatus.OUT_OF_SERVICE
        )

        return WarehouseStatusResult(
            warehouseId = warehouse.id,
            warehouseName = warehouse.name,
            status = WarehouseStatus.OUT_OF_SERVICE
        )
    }
}

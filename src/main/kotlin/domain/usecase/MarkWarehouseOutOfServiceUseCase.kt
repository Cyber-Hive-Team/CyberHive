package org.example.domain.usecase

import org.example.domain.model.WarehouseStatus
import org.example.domain.model.result.WarehouseStatusResult
import org.example.domain.repository.WarehouseRepository
import org.example.domain.repository.WarehouseStatusRepository

class MarkWarehouseOutOfServiceUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseStatusRepository: WarehouseStatusRepository
) {

    suspend operator fun invoke(warehouseId: String): Result<WarehouseStatusResult> {
        return runCatching {
            val warehouse = warehouseRepository.getById(warehouseId)
                .getOrThrow()
            warehouseStatusRepository.updateStatus(
                warehouseId = warehouse.id,
                status = WarehouseStatus.OUT_OF_SERVICE
            )
            WarehouseStatusResult(
                warehouseId = warehouse.id,
                warehouseName = warehouse.name,
                status = WarehouseStatus.OUT_OF_SERVICE
            )
        }
    }
}

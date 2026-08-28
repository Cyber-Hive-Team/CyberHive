package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.repository.WarehouseRepository

class AssignPackageToCargoQueueUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    operator fun invoke(
        warehouseId: String,
        cargoPackage: Package
    ): Boolean {
        val added = warehouseRepository.addPackageToCargoQueue(
            warehouseId,
            cargoPackage
        )

        if (!added) {
            return false
        }

        return warehouseRepository.sortCargoQueue(
            warehouseId
        )
    }
}

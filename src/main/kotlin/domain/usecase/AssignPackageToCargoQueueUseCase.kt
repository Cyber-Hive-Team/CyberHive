package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.repository.WarehouseRepository

class AssignPackageToCargoQueueUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(
        warehouseId: String,
        cargoPackage: Package
    ): Boolean {
        val alreadyExists =
            warehouseRepository.isPackageInCargoQueue(
                warehouseId,
                cargoPackage.id
            )

        if (alreadyExists) {
            return false
        }
        val added = warehouseRepository.addPackageToCargoQueue(
            warehouseId,
            cargoPackage
        )

        return if (!added) {
            false
        } else {
            warehouseRepository.sortCargoQueue(warehouseId)
        }
    }
}

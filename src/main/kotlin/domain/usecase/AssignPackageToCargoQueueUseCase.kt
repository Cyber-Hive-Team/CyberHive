package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.repository.WarehouseRepository

class AssignPackageToCargoQueueUseCase(
    private val warehouseRepository: WarehouseRepository
) {
    suspend operator fun invoke(
        warehouseId: String,
        cargoPackage: Package
    ): Result<Boolean> {
        return runCatching {
            val alreadyExists =
                warehouseRepository.isPackageInCargoQueue(
                    warehouseId,
                    cargoPackage.id
                ).getOrThrow()
            if (alreadyExists) {
                false
            } else {
                val added = warehouseRepository.addPackageToCargoQueue(
                    warehouseId,
                    cargoPackage
                ).getOrThrow()
                if (added) {
                    warehouseRepository.sortCargoQueue(warehouseId).getOrThrow()
                } else {
                    false
                }
            }
        }
    }
}

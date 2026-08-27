package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Warehouse

class AssignPackageToCargoQueueUseCase {

    operator fun invoke(
        warehouse: Warehouse,
        cargoPackage: Package
    ) {
        val alreadyExists = warehouse
            .getCargoQueue()
            .any { it.id == cargoPackage.id }

        if (alreadyExists) {
            return
        }

        warehouse.addPackages(
            listOf(cargoPackage)
        )

        warehouse.sortCargoQueue()
    }
}

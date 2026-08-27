package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Warehouse

class ReroutePackageUseCase {

    operator fun invoke(
        packageItem: Package,
        newDestination: Warehouse
    ): Boolean {

        val reroutedPackage = packageItem.copy(
            destinationWarehouse = newDestination
        )

        return packageItem.originWarehouse.replacePackageInQueue(
            packageId = packageItem.id,
            updatedPackage = reroutedPackage
        )
    }
}
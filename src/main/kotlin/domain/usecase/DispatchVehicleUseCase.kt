package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val packageRepository: PackageRepository
) {

    operator fun invoke(vehicleId: String): List<Package> {

        val vehicle = vehicleRepository.getVehicles().data
            .firstOrNull { it.id == vehicleId }
            ?: return emptyList()

        val availablePackages = packageRepository.getAllPackages().data
            .filter { packageItem ->
                packageItem.originWarehouse.id == vehicle.currentHub.id
            }

        val loadedPackages = mutableListOf<Package>()

        availablePackages.fold(0.0) { currentWeight, packageItem ->

            if (currentWeight + packageItem.weight <= vehicle.maxCapacityKg) {
                loadedPackages.add(packageItem)
                currentWeight + packageItem.weight
            } else {
                currentWeight
            }
        }

        return loadedPackages
    }
}
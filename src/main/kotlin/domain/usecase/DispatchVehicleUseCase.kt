package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.model.result.Result
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository

class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val packageRepository: PackageRepository
) {

    operator fun invoke(vehicleId: String): Result<List<Package>> {

        val vehicle = vehicleRepository.getVehicles().data
            .firstOrNull { it.id == vehicleId }
            ?: throw VehicleNotFoundException()

        val availablePackages = packageRepository.getAllPackages().data
            .filter { packageItem ->
                packageItem.originWarehouse.id == vehicle.currentHub.id
            }

        val loadedPackages = availablePackages.fold(
            initial = Pair(0.0, emptyList<Package>())
        ) { (currentWeight, packages), packageItem ->

            if (currentWeight + packageItem.weight <= vehicle.maxCapacityKg) {
                Pair(
                    currentWeight + packageItem.weight,
                    packages + packageItem
                )
            } else {
                Pair(currentWeight, packages)
            }
        }

        return Result(loadedPackages.second)
    }
}

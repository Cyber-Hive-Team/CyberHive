package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository


class DispatchVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val packageRepository: PackageRepository
) {

    operator fun invoke(
        vehicleId: String
    ): Result<List<Package>> {

        return runCatching {

            val vehicle =
                vehicleRepository
                    .getVehicles()
                    .getOrThrow()
                    .firstOrNull {
                        it.id == vehicleId
                    }
                    ?: throw VehicleNotFoundException()


            loadPackagesForVehicle(vehicle)
        }
    }


    private fun loadPackagesForVehicle(
        vehicle: Vehicle
    ): List<Package> {

        val availablePackages =
            loadAvailablePackages(vehicle)


        return calculateLoadedPackages(
            vehicle,
            availablePackages
        )
    }


    private fun loadAvailablePackages(
        vehicle: Vehicle
    ): List<Package> {

        return packageRepository
            .getAllPackages()
            .getOrThrow()
            .filter { packageItem ->

                packageItem.originWarehouse.id ==
                        vehicle.currentHub.id
            }
    }


    private fun calculateLoadedPackages(
        vehicle: Vehicle,
        availablePackages: List<Package>
    ): List<Package> {

        val loadedPackages =
            availablePackages.fold(
                initial = Pair(
                    0.0,
                    emptyList<Package>()
                )
            ) { (currentWeight, packages), packageItem ->

                if (
                    currentWeight + packageItem.weight <=
                    vehicle.maxCapacityKg
                ) {

                    Pair(
                        currentWeight + packageItem.weight,
                        packages + packageItem
                    )

                } else {

                    Pair(
                        currentWeight,
                        packages
                    )
                }
            }


        return loadedPackages.second
    }
}

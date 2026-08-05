package org.example.domain.routing.service

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.routing.calculator.RingSlotCalculator
import org.example.domain.routing.ring.ConsistentHashingRing

class ConsistentHashVehicleRoutingService {

    private val packageRingPositionCalculator = RingSlotCalculator()
    private val vehicleRing = ConsistentHashingRing()

    fun assignPackagesToVehicles(
        packages: List<Package>,
        vehicles: List<Vehicle>
    ): Map<Vehicle, List<Package>> {

        initializeVehicleRing(vehicles)

        val vehiclePackageAllocation =
            mutableMapOf<Vehicle, MutableList<Package>>()

        packages.forEach { deliveryPackage ->

            val destinationVehicle =
                resolvePackageDestinationVehicle(deliveryPackage)

            destinationVehicle?.let {
                vehiclePackageAllocation
                    .getOrPut(it) { mutableListOf() }
                    .add(deliveryPackage)
            }
        }

        return vehiclePackageAllocation
    }


    private fun initializeVehicleRing(
        vehicles: List<Vehicle>
    ) {
        vehicleRing.addVehicleAtSlot(
            15,
            findVehicleByIdentifier(vehicles, "TRK-001")
        )

        vehicleRing.addVehicleAtSlot(
            40,
            findVehicleByIdentifier(vehicles, "TRK-002")
        )

        vehicleRing.addVehicleAtSlot(
            65,
            findVehicleByIdentifier(vehicles, "TRK-003")
        )

        vehicleRing.addVehicleAtSlot(
            90,
            findVehicleByIdentifier(vehicles, "TRK-004")
        )
    }


    private fun findVehicleByIdentifier(
        vehicles: List<Vehicle>,
        vehicleId: String
    ): Vehicle {
        return vehicles.first { it.id == vehicleId }
    }


    private fun resolvePackageDestinationVehicle(
        deliveryPackage: Package
    ): Vehicle? {

        val packageRingSlot =
            packageRingPositionCalculator.calculateSlot(
                deliveryPackage.id
            )

        return vehicleRing
            .findNextVehicleClockwise(packageRingSlot)
    }

    fun redistributePackagesAfterVehicleFailure(
        currentVehiclePackageAllocation: Map<Vehicle, List<Package>>,
        failedVehicleId: String,
        vehicles: List<Vehicle>
    ): Map<Vehicle, List<Package>> {
        val updatedVehiclePackageAllocation =
            cloneVehiclePackageAllocation(
                currentVehiclePackageAllocation
            )
        val failedVehiclePackages =
            extractPackagesAssignedToFailedVehicle(
                currentVehiclePackageAllocation,
                failedVehicleId
            )
        clearFailedVehicleAllocation(
            updatedVehiclePackageAllocation,
            failedVehicleId,
            vehicles
        )
        reallocatePackagesToAvailableVehicles(
            updatedVehiclePackageAllocation,
            failedVehiclePackages
        )
        return updatedVehiclePackageAllocation
    }

    private fun cloneVehiclePackageAllocation(
        allocation: Map<Vehicle, List<Package>>
    ): MutableMap<Vehicle, MutableList<Package>> {

        return allocation.mapValues {
            it.value.toMutableList()
        }.toMutableMap()
    }

    private fun extractPackagesAssignedToFailedVehicle(
        allocation: Map<Vehicle, List<Package>>,
        failedVehicleId: String
    ): List<Package> {
        return allocation
            .filterKeys { it.id == failedVehicleId }
            .values
            .flatten()
    }

    private fun clearFailedVehicleAllocation(
        allocation: MutableMap<Vehicle, MutableList<Package>>,
        failedVehicleId: String,
        vehicles: List<Vehicle>
    ) {
        vehicles.find { it.id == failedVehicleId }
            ?.let { allocation[it]?.clear() }
    }


    private fun reallocatePackagesToAvailableVehicles(
        allocation: MutableMap<Vehicle, MutableList<Package>>,
        packages: List<Package>
    ) {
        packages.forEach { deliveryPackage ->

            val destinationVehicle =
                resolvePackageDestinationVehicle(deliveryPackage)

            destinationVehicle?.let {
                allocation
                    .getOrPut(it) { mutableListOf() }
                    .add(deliveryPackage)
            }
        }
    }
}
package org.example.domain.routing.service

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.routing.ring.ConsistentHashingRing

class VehicleFailureHandler(
    private val vehicleRing: ConsistentHashingRing
) {

    fun redistributePackagesAfterVehicleFailure(
        currentAllocation:
        Map<Vehicle, List<Package>>,
        failedVehicleId: String,
        failedVehicleSlot: Int
    ): Map<Vehicle, List<Package>> {
        val updatedAllocation =
            cloneAllocation(currentAllocation)

        val failedPackages =
            extractFailedVehiclePackages(
                currentAllocation,
                failedVehicleId
            )

        clearFailedVehiclePackages(
            updatedAllocation,
            failedVehicleId
        )

        movePackagesToNextVehicle(
            allocation = updatedAllocation,
            packages = failedPackages,
            failedVehicleSlot = failedVehicleSlot
        )

        return updatedAllocation
    }

    private fun cloneAllocation(
        allocation: Map<Vehicle, List<Package>>
    ): MutableMap<Vehicle, MutableList<Package>> {
        return allocation
            .mapValues {
                it.value.toMutableList()
            }
            .toMutableMap()
    }

    private fun extractFailedVehiclePackages(
        allocation: Map<Vehicle, List<Package>>,
        failedVehicleId: String
    ): List<Package> {
        return allocation
            .filterKeys {
                it.id == failedVehicleId
            }
            .values
            .flatten()
    }

    private fun clearFailedVehiclePackages(
        allocation:
        MutableMap<Vehicle, MutableList<Package>>,
        failedVehicleId: String
    ) {
        allocation
            .entries
            .firstOrNull {
                it.key.id == failedVehicleId
            }
            ?.value
            ?.clear()
    }

    private fun movePackagesToNextVehicle(
        allocation:
        MutableMap<Vehicle, MutableList<Package>>,
        packages: List<Package>,
        failedVehicleSlot: Int
    ) {
        val nextVehicle =
            vehicleRing.findNextVehicleClockwise(
                failedVehicleSlot + 1
            ) ?: return

        allocation
            .getOrPut(nextVehicle) {
                mutableListOf()
            }
            .addAll(packages)
    }
}
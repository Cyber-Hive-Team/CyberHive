package org.example.domain.routing.service

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.routing.calculator.RingSlotCalculator
import org.example.domain.routing.ring.ConsistentHashingRing

class ConsistentHashVehicleRoutingService {
    private val packageRingPositionCalculator = RingSlotCalculator()
    private val vehicleRing = ConsistentHashingRing()
    private val failureHandler = VehicleFailureHandler(vehicleRing)

    fun assignPackagesToVehicles(
        packages: List<Package>,
        vehicles: List<Vehicle>
    ): Map<Vehicle, List<Package>> {
        initializeVehicleRing(vehicles)

        val allocation =
            mutableMapOf<Vehicle, MutableList<Package>>()

        packages.forEach { deliveryPackage ->
            val destinationVehicle =
                resolvePackageDestinationVehicle(
                    deliveryPackage
                )

            destinationVehicle?.let { vehicle ->
                allocation
                    .getOrPut(vehicle) {
                        mutableListOf()
                    }
                    .add(deliveryPackage)
            }
        }

        return allocation
    }

    private fun initializeVehicleRing(
        vehicles: List<Vehicle>
    ) {
        require(vehicles.size >= 4) {
            "At least 4 vehicles are required"
        }

        val selectedVehicles =
            vehicles.take(4)

        val vehicleSlots =
            listOf(15, 40, 65, 90)

        vehicleSlots
            .zip(selectedVehicles)
            .forEach { (slot, vehicle) ->
                vehicleRing.addVehicleAtSlot(
                    slot,
                    vehicle
                )
            }
    }
    private fun resolvePackageDestinationVehicle(
        deliveryPackage: Package
    ): Vehicle? {
        val packageRingSlot =
            packageRingPositionCalculator
                .calculateSlot(deliveryPackage.id)

        return vehicleRing
            .findNextVehicleClockwise(
                packageRingSlot
            )
    }

    fun handleVehicleFailure(
        currentAllocation: Map<Vehicle, List<Package>>,
        failedVehicleId: String,
        failedVehicleSlot: Int
    ): Map<Vehicle, List<Package>> {
        return failureHandler.redistributePackagesAfterVehicleFailure(
            currentAllocation = currentAllocation,
            failedVehicleId = failedVehicleId,
            failedVehicleSlot = failedVehicleSlot
        )
    }

    fun getVehiclesBySlot(): Map<Int, Vehicle> =
        vehicleRing.getVehiclesBySlot()
}
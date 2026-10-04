package org.example.domain.dispatch

import org.example.domain.model.Package
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.state.CreatedState

abstract class BaseDispatchProcessor (
    private val capacityReservations: VehicleCapacityReservations
){

    fun dispatch(vehicle: Vehicle, cargo: List<Package>) {
        validateCargo(vehicle, cargo)
        reserveVehicleCapacity(vehicle, cargo)
        updateShipmentState(cargo)
        notifyDispatchStatus(vehicle, cargo)
    }

    protected fun validateCargo(
        vehicle: Vehicle,
        cargo: List<Package>
    ){
        if (cargo.isEmpty()) {
            throw EntityValidationException(
                "Cargo cannot be empty"
            )
        }
        val packageIds = cargo.map { it.id }

        if (packageIds.distinct().size != packageIds.size) {
            throw EntityValidationException(
                "Cargo contains duplicate packages"
            )
        }

        cargo.forEach { cargoPackage ->

            if (!cargoPackage.weight.isFinite() ||
                cargoPackage.weight <= 0.0
            ) {
                throw EntityValidationException(
                    "Invalid weight for package ${cargoPackage.id}"
                )
            }

            if (cargoPackage.originWarehouse.id != vehicle.currentHub.id) {
                throw EntityValidationException(
                    "Package ${cargoPackage.id} is at another warehouse"
                )
            }

            if (cargoPackage.state !is CreatedState) {
                throw EntityValidationException(
                    "Package ${cargoPackage.id} is already dispatched"
                )
            }
        }
    }

    protected fun reserveVehicleCapacity(
        vehicle: Vehicle,
        cargo: List<Package>
    ) {
        val totalWeight = cargo.sumOf { it.weight }

        capacityReservations.reserve(
            vehicle = vehicle,
            cargoWeight = totalWeight
        )
    }

    protected abstract fun updateShipmentState(
        cargo: List<Package>
    )

    protected open fun notifyDispatchStatus(
        vehicle: Vehicle,
        cargo: List<Package>
    ) {
        // سنخصص الإشعار لاحقًا.
    }
}

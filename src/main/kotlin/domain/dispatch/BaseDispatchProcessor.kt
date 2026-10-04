package org.example.domain.dispatch

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

abstract class BaseDispatchProcessor {

    fun dispatch(vehicle: Vehicle, cargo: List<Package>) {
        validateCargo(vehicle, cargo)
        reserveVehicleCapacity(vehicle, cargo)
        updateShipmentState(cargo)
        notifyDispatchStatus(vehicle, cargo)
    }

    protected abstract fun validateCargo(
        vehicle: Vehicle,
        cargo: List<Package>
    )

    protected abstract fun reserveVehicleCapacity(
        vehicle: Vehicle,
        cargo: List<Package>
    )

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

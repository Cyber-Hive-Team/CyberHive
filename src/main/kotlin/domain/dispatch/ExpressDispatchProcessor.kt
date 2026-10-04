package org.example.domain.dispatch

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

class ExpressDispatchProcessor(
    capacityReservations: VehicleCapacityReservations
) : BaseDispatchProcessor(capacityReservations) {

    override fun notifyDispatchStatus(
        vehicle: Vehicle,
        cargo: List<Package>
    ) {
        println(
            "EXPRESS: ${cargo.size} packages dispatched " +
                    "on vehicle ${vehicle.id}; express service requested"
        )
    }
}

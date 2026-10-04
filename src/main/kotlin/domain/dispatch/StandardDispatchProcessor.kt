package org.example.domain.dispatch

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

class StandardDispatchProcessor(
    capacityReservations: VehicleCapacityReservations
) : BaseDispatchProcessor(capacityReservations)  {

    override fun notifyDispatchStatus(
        vehicle: Vehicle,
        cargo: List<Package>
    ) {
        println(
            "STANDARD: ${cargo.size} packages dispatched " +
                    "on vehicle ${vehicle.id}"
        )
    }
}

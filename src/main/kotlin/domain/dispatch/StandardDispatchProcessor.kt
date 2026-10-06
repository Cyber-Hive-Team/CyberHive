package org.example.domain.dispatch

import org.example.domain.model.Package
import org.example.domain.model.Vehicle

class StandardDispatchProcessor(
    capacityReservations: VehicleCapacityReservations,
    private val notifier: DispatchNotifier
) : BaseDispatchProcessor(capacityReservations)  {

    override fun notifyDispatchStatus(
        vehicle: Vehicle,
        cargo: List<Package>
    ) {
        notifier.notify(
            "STANDARD: ${cargo.size} packages dispatched " +
                    "on vehicle ${vehicle.id}"
        )
    }
}

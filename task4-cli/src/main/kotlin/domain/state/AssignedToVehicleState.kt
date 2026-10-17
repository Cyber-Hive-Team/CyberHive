package org.example.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException

class AssignedToVehicleState : PackageState {

    override fun assignToVehicle(): PackageState {
        throw InvalidPackageStateTransitionException(
            "The package is already assigned to a vehicle."
        )
    }

    override fun startTransit(): PackageState {
        return InTransitState()
    }

    override fun markDelivered(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A package must be in transit before being marked as delivered."
        )
    }

    override fun markDeliveryFailed(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A package must be in transit before being marked as delivery failed."
        )
    }
}

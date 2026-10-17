package org.example.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException

class InTransitState : PackageState {

    override fun assignToVehicle(): PackageState {
        throw InvalidPackageStateTransitionException(
            "An in-transit package cannot be assigned to another vehicle."
        )
    }

    override fun startTransit(): PackageState {
        throw InvalidPackageStateTransitionException(
            "The package is already in transit."
        )
    }

    override fun markDelivered(): PackageState {
        return DeliveredState()
    }

    override fun markDeliveryFailed(): PackageState {
        return DeliveryFailedState()
    }
}

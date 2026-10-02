package org.example.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException

class DeliveredState : PackageState {

    override fun assignToVehicle(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A delivered package cannot be assigned to a vehicle."
        )
    }

    override fun startTransit(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A delivered package cannot start transit."
        )
    }

    override fun markDelivered(): PackageState {
        throw InvalidPackageStateTransitionException(
            "The package is already delivered."
        )
    }

    override fun markDeliveryFailed(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A delivered package cannot be marked as delivery failed."
        )
    }
}

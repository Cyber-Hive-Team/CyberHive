package org.example.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException

class DeliveryFailedState : PackageState {

    override fun assignToVehicle(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A package with failed delivery cannot be assigned to a vehicle."
        )
    }

    override fun startTransit(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A package with failed delivery cannot start transit."
        )
    }

    override fun markDelivered(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A package with failed delivery cannot be marked as delivered."
        )
    }

    override fun markDeliveryFailed(): PackageState {
        throw InvalidPackageStateTransitionException(
            "The package is already marked as delivery failed."
        )
    }
}

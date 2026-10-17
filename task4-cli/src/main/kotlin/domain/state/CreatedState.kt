package org.example.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException

class CreatedState : PackageState {

    override fun assignToVehicle(): PackageState {
        return AssignedToVehicleState()
    }

    override fun startTransit(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A created package cannot start transit before being assigned to a vehicle."
        )
    }

    override fun markDelivered(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A created package cannot be marked as delivered."
        )
    }

    override fun markDeliveryFailed(): PackageState {
        throw InvalidPackageStateTransitionException(
            "A created package cannot be marked as delivery failed."
        )
    }
}

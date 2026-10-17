package org.example.domain.state

interface PackageState {

    fun assignToVehicle(): PackageState

    fun startTransit(): PackageState

    fun markDelivered(): PackageState

    fun markDeliveryFailed(): PackageState
}

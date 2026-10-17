package org.example.domain.model

import org.example.domain.model.exception.InvalidPackageIdException
import org.example.domain.state.CreatedState
import org.example.domain.state.PackageState

private const val DEFAULT_BASE_RATE = 10.0
private const val PACKAGE_ID_PREFIX = "^PKG-\\d{6}$"

data class Package(
    val id: String,
    val weight: Double,
    val priority: Priority,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val baseRate: Double = DEFAULT_BASE_RATE,
    val volumeM3: Double = 0.0

) : PackageComponent {

    private var currentState: PackageState = CreatedState()

    val state: PackageState
        get() = currentState

    override fun calculateTransitRate(): Double {
        return weight * baseRate
    }
    override fun getDescription(): String {
        return "Standard Package (ID: $id, Weight: $weight kg)"
    }

    init {
        validateId()
    }

    private fun validateId() {
        if (!id.matches(Regex(PACKAGE_ID_PREFIX))) {
            throw InvalidPackageIdException()
        }
    }

    fun assignToVehicle() {
        currentState = currentState.assignToVehicle()
    }

    fun startTransit() {
        currentState = currentState.startTransit()
    }

    fun markDelivered() {
        currentState = currentState.markDelivered()
    }

    fun markDeliveryFailed() {
        currentState = currentState.markDeliveryFailed()
    }

}

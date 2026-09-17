package org.example.domain.model

import org.example.domain.model.exception.InvalidPackageIdException

private const val DEFAULT_BASE_RATE = 10.0
private const val PACKAGE_ID_PREFIX = "PKG-\\d{6}$"

data class Package(
    val id: String,
    val weight: Double,
    val priority: Priority,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
    val baseRate: Double = DEFAULT_BASE_RATE
): PackageComponent {
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
}

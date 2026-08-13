package org.example.domain.model

private const val DEFAULT_BASE_RATE = 10.0
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
}

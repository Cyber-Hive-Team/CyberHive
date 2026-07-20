package org.example.Data
data class FleetRaw(
    val vehicleId: String,
    val currentHubId: String,
    val maxCapacityKg: String?,
    val costPerKm: String?
)
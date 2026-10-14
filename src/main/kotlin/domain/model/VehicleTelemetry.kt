package org.example.domain.model

data class VehicleTelemetry(
    val vehicleId: String,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double,
    val fuelPercentage: Double,
    val timestampMillis: Long
)

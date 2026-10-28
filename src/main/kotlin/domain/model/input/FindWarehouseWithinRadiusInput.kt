package org.example.domain.model.input

data class FindWarehouseWithinRadiusInput(
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Double
)

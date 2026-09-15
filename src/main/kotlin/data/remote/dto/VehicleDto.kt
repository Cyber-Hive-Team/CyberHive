package org.example.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleDto(
    @SerialName("vehicleId")
    val vehicleId: String,

    @SerialName("currentHubId")
    val currentHubId: String? = null,

    @SerialName("maxCapacityKg")
    val maxCapacityKg: Double? = null,

    @SerialName("costPerKm")
    val costPerKm: Double? = null
)

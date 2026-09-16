package org.example.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponseDto(
    @SerialName("vehicleId")
    val vehicleId: String,

    @SerialName("currentHubId")
    val currentHubId: String? = null,

    @SerialName("maxCapacityKg")
    val maxCapacityKg: Double? = null,

    @SerialName("costPerKm")
    val costPerKm: Double? = null
)

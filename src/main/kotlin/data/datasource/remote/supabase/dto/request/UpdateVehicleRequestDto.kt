package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateVehicleRequestDto(
    @SerialName("currentHubId")
    val currentHubId: String? = null,

    @SerialName("maxCapacityKg")
    val maxCapacityKg: Double? = null,

    @SerialName("costPerKm")
    val costPerKm: Double? = null
)

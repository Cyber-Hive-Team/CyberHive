package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateVehicleRequestDto(
    @SerialName("current_hub_id")
    val currentHubId: String? = null,

    @SerialName("max_capacity_kg")
    val maxCapacityKg: Double? = null,

    @SerialName("cost_per_km")
    val costPerKm: Double? = null
)

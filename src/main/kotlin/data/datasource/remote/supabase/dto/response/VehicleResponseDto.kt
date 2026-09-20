package org.example.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponseDto(
    @SerialName("vehicle_id")
    val vehicleId: String,

    @SerialName("current_hub_id")
    val currentHubId: String,

    @SerialName("max_capacity_kg")
    val maxCapacityKg: Double? = null,

    @SerialName("cost_per_km")
    val costPerKm: Double? = null
)

package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRouteRequestDto(
    @SerialName("origin_hub_id")
    val originHubId: String? = null,

    @SerialName("destination_hub_id")
    val destinationHubId: String? = null,

    @SerialName("distance_km")
    val distanceKm: Double? = null,

    @SerialName("typical_delay_min")
    val typicalDelayMin: Int? = null
)

package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRouteRequestDto(
    @SerialName("originHubId")
    val originHubId: String? = null,

    @SerialName("destinationHubId")
    val destinationHubId: String? = null,

    @SerialName("distanceKm")
    val distanceKm: Double? = null,

    @SerialName("typicalDelayMin")
    val typicalDelayMin: Int? = null
)

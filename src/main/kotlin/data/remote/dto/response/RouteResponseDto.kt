package org.example.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteResponseDto(
    @SerialName("routeId")
    val routeId: String,

    @SerialName("originHubId")
    val originHubId: String,

    @SerialName("destinationHubId")
    val destinationHubId: String,

    @SerialName("distanceKm")
    val distanceKm: Double,

    @SerialName("typicalDelayMin")
    val typicalDelayMin: Int? = null
)

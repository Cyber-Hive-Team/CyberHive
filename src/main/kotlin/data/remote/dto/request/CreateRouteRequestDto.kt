package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRouteRequestDto(
    @SerialName("routeId")
    val routeId: String,

    @SerialName("originHubId")
    val originHubId: String,

    @SerialName("destinationHubId")
    val destinationHubId: String,

    @SerialName("distanceKm")
    val distanceKm: Double,

    @SerialName("typicalDelayMinutes")
    val typicalDelayMinutes: Int
)

package org.example.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RouteDto(
    @SerialName("routeId")
    val routeId: String,

    @SerialName("originHubId")
    val originHubId: String,

    @SerialName("destinationHubId")
    val destinationHubId: String,

    @SerialName("distanceKm")
    val distanceKm: Double,

    @SerialName("typicalDelayMinutes")
    val typicalDelayMinutes: Int? = null
)

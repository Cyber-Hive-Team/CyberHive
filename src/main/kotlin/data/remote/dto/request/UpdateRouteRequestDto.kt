package data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateRouteRequestDto(
    @SerialName("routeId")
    val routeId: String,

    @SerialName("originHubId")
    val originHubId: String? = null,

    @SerialName("destinationHubId")
    val destinationHubId: String? = null,

    @SerialName("distanceKm")
    val distanceKm: Double? = null,

    @SerialName("typicalDelayMinutes")
    val typicalDelayMinutes: Int? = null
)

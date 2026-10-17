package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePackageRequestDto(
    @SerialName("weight")
    val weight: Double? = null,
    @SerialName("origin_hub_id")
    val originHubId: String,
    @SerialName("destination_hub_id")
    val destinationHubId: String,
    @SerialName("priority")
    val priority: String? = null
)

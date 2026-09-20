package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePackageRequestDto(
    @SerialName("weight")
    val weight: Double? = null,
    @SerialName("originHubId")
    val originHubId: String,
    @SerialName("destinationHubId")
    val destinationHubId: String,
    @SerialName("priority")
    val priority: String? = null
)

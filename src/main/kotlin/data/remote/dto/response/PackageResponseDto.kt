package org.example.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable

data class PackageResponseDto(
    @SerialName("id")
    val id: String,
    @SerialName("weight")
    val weight: Double,
    @SerialName("originHubId")
    val originHubId: String,
    @SerialName("destinationHubId")
    val destinationHubId: String,
    @SerialName("priority")
    val priority: String
)

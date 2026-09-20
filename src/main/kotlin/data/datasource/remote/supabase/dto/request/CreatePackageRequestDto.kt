package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreatePackageRequestDto(
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

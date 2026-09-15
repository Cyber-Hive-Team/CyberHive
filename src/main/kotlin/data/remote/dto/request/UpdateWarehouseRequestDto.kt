package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateWarehouseRequestDto(
    @SerialName("name")
    val name: String,
    @SerialName("regionalZone")
    val regionalZone: String,
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null
)

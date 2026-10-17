package org.example.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WarehouseResponseDto(
    @SerialName("warehouse_id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("regional_zone")
    val regionalZone: String?,
    @SerialName("latitude")
    val latitude: Double?,
    @SerialName("longitude")
    val longitude: Double?
)

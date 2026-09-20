package org.example.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateWarehouseRequestDto(
    @SerialName("warehouse_id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("regional_zone")
    val regionalZone: String,
    @SerialName("latitude")
    val latitude: Double,
    @SerialName("longitude")
    val longitude: Double
)

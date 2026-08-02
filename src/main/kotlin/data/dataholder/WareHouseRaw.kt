package org.example.data.dataholder

import org.example.domain.model.RegionalZone

data class WareHouseRaw(
    val id: String,
    val name: String,
    val regionalZone: RegionalZone,
    val latitude: Double,
    val longitude: Double
)
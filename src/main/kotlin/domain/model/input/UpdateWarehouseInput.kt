package org.example.domain.model.input

import org.example.domain.model.RegionalZone

data class UpdateWarehouseInput(
    val id: String,
    val name: String? = null,
    val regionalZone: RegionalZone? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

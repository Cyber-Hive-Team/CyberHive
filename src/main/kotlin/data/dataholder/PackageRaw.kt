package org.example.data.dataholder

import org.example.domain.model.Priority

data class PackageRaw(
    val id: String,
    val weight: Double,
    val originHubId: String,
    val destinationHubId: String,
    val priority: Priority
)
package org.example.data.dataholder

data class PackageRaw(
    val id: String,
    val weight: Double,
    val originHubId: String,
    val destinationHubId: String,
    val priority: Priority
)
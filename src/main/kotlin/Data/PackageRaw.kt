package org.example.Data

import org.example.Data.Priority

data class PackageRaw(
    val id: String,
    val weight: Double,
    val destinationHubId: String,
    val priority: Priority
)
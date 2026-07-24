package org.example.data.dataHolder

import org.example.data.dataHolder.Priority

data class PackageRaw(
    val id: String,
    val weight: Double,
    val destinationHubId: String,
    val priority: Priority
)
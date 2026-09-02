package org.example.domain.model

import kotlin.time.Instant

data class PackageDeliveryTime(
    val packageId: String,
    val expectedArrivalTime: Instant,
    val actualArrivalTime: Instant
)

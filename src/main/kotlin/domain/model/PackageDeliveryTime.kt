package org.example.domain.model

import java.time.LocalDateTime

data class PackageDeliveryTime(
    val packageId: String,
    val expectedArrivalTime: LocalDateTime,
    val actualArrivalTime: LocalDateTime
)

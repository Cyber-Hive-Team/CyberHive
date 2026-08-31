package org.example.domain.model

import java.time.LocalDateTime

data class PackageWarehouseStay(
    val packageId: String,
    val arrivedAt: LocalDateTime
)

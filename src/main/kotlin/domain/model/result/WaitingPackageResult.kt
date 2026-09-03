package org.example.domain.model.result

data class WaitingPackageResult(
    val packageId: String,
    val waitingHours: Long
)

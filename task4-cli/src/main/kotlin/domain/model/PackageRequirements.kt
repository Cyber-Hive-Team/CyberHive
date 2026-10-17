package org.example.domain.model

data class PackageRequirements(
    val packageId: String,
    val isFragile: Boolean,
    val requiresColdStorage: Boolean,
    val requiresSpecialHandling: Boolean
)

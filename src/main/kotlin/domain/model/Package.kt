package org.example.domain.model

import org.example.domain.model.Priority

data class Package(
    val id: String,
    val weight: Double,
    val priority: Priority,
    val origin: Warehouse,
    val destination: Warehouse
)

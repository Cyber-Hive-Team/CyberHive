package org.example.domain.model
import org.example.data.dataholder.Priority

class Package(
    val id: String,
    val weight: Double,
    val priority: Priority,
    val origin: Warehouse,
    val destination: Warehouse
)

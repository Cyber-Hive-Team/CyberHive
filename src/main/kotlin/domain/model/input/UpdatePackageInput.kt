package org.example.domain.model.input

import org.example.domain.model.Priority
import org.example.domain.model.Warehouse

data class UpdatePackageInput(
    val id: String,
    val weight: Double? = null,
    val priority: Priority? = null,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse,
)

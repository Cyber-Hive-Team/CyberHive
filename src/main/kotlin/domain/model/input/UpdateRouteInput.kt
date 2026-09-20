package org.example.domain.model.input

import org.example.domain.model.Warehouse

data class UpdateRouteInput(
    val id: String,
    val originWarehouse: Warehouse? = null,
    val destinationWarehouse: Warehouse? = null,
    val distanceKm: Double? = null,
    val typicalDelayMin: Int? = null
)

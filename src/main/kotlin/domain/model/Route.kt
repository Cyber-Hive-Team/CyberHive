package org.example.domain.model

data class Route(
    val id: String,
    val distanceKm: Double,
    val typicalDelayMin: Int,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
)

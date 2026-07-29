package org.example.domain.model

class Route(
    val routeId: String,
    val distanceKm: Double,
    val typicalDelayMin: Int,
    val origin: Warehouse,
    val destination: Warehouse
)

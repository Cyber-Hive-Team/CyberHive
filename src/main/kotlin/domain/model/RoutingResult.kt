package org.example.domain.model

data class RoutingResult(
    val path: List<Warehouse>,
    val distanceKm: Double
)
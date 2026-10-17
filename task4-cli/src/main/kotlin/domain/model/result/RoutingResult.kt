package org.example.domain.model.result

import org.example.domain.model.Warehouse

data class RoutingResult(
    val path: List<Warehouse>,
    val distanceKm: Double
)

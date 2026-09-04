package org.example.domain.model.input

data class FindFewestHopsRouteInput(
    val startWarehouseId: String,
    val destinationWarehouseId: String
)

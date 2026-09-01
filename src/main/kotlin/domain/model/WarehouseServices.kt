package org.example.domain.model

data class WarehouseServices(
    val warehouseId: String,
    val supportsFragileHandling: Boolean,
    val supportsColdStorage: Boolean,
    val supportsSpecialHandling: Boolean
)

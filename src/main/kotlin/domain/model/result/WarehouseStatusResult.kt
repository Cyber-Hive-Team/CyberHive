package org.example.domain.model.result

import org.example.domain.model.WarehouseStatus

data class WarehouseStatusResult(
    val warehouseId: String,
    val warehouseName: String,
    val status: WarehouseStatus
)

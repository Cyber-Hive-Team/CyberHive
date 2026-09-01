package org.example.domain.repository

import org.example.domain.model.WarehouseStatus

interface WarehouseStatusRepository {

    fun getStatus(warehouseId: String): WarehouseStatus

    fun updateStatus(
        warehouseId: String,
        status: WarehouseStatus
    ): Boolean
}

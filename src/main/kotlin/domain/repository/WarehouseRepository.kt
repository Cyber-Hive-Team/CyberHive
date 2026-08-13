package org.example.domain.repository

import org.example.domain.model.Warehouse

data class WarehouseRepositoryResult(
    val warehouses: List<Warehouse>,
    val warnings: List<String>
)

interface WarehouseRepository {
    fun getAllWarehouses(): WarehouseRepositoryResult
}

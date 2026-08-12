package org.example.domain.repository

import org.example.domain.model.Warehouse

interface WarehouseRepository {
    fun getAllWarehouses(): List<Warehouse>
}
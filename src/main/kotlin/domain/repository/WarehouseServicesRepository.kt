package org.example.domain.repository

import org.example.domain.model.WarehouseServices

interface WarehouseServicesRepository {
    fun getAllWarehouseServices(): List<WarehouseServices>
}
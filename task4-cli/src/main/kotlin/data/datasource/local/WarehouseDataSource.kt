package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.WarehouseRaw

interface WarehouseDataSource {
    suspend fun getWarehouses(): List<RawResult<WarehouseRaw>>
    suspend fun replaceAll(warehouses: List<WarehouseRaw>)
}


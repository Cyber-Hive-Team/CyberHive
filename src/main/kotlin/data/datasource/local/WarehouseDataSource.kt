package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.WarehouseRaw

interface WarehouseDataSource {
    fun getWarehouses(): List<RawResult<WarehouseRaw>>
}


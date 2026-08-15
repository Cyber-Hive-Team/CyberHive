package org.example.data.datasource

import org.example.data.dataholder.WarehouseDataSourceResult

interface WarehouseDataSource {
    fun getWarehouses(): WarehouseDataSourceResult
}
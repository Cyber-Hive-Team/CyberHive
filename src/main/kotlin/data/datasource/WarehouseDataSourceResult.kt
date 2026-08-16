package org.example.data.datasource

import org.example.data.dataholder.WareHouseRaw

data class WarehouseDataSourceResult(
    val warehouses: List<WareHouseRaw>,
    val warnings: List<String>
)
package org.example.data.dataholder

data class WarehouseDataSourceResult(
    val warehouses: List<WareHouseRaw>,
    val warnings: List<String>
)

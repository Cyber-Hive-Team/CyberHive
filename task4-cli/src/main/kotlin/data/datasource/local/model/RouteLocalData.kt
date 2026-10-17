package org.example.data.datasource.local.model

import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.WarehouseRaw

data class RouteLocalData(
    val routeRaw: RouteRaw,
    val originWarehouse: WarehouseRaw,
    val destinationWarehouse: WarehouseRaw
)

package org.example.data.datasource.local.model

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.WarehouseRaw

data class PackageLocalData(
    val packageRaw: PackageRaw,
    val originWarehouse: WarehouseRaw,
    val destinationWarehouse: WarehouseRaw
)

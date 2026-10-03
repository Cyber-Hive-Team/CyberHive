package org.example.data.datasource.local.model

import org.example.data.dataholder.VehicleRaw
import org.example.data.dataholder.WarehouseRaw

data class VehicleLocalData(
    val vehicleRaw: VehicleRaw,
    val currentWarehouse: WarehouseRaw
)

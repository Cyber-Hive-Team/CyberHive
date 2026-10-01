package org.example.data.mapper.csv

import org.example.data.dataholder.WarehouseRaw
import org.example.domain.model.Warehouse


fun WarehouseRaw.toDomainModel(): Warehouse {
        return Warehouse(
            id = id,
            name = name,
            regionalZone = regionalZone,
            latitude = latitude!!,
            longitude = longitude!!
        )
    }


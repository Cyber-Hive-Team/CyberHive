package org.example.data.mapper.csv

import org.example.data.dataholder.WarehouseRaw
import org.example.domain.model.Warehouse

class WarehouseMapper {

    fun map(raw: WarehouseRaw): Warehouse {
        return Warehouse(
            id = raw.id,
            name = raw.name,
            regionalZone = raw.regionalZone,
            latitude = raw.latitude!!,
            longitude = raw.longitude!!
        )
    }
}

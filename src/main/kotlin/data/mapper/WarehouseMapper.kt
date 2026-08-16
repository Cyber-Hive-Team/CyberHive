package org.example.data.mapper

import org.example.data.dataholder.WareHouseRaw
import org.example.domain.model.Warehouse

class WarehouseMapper {

    fun map(raw: WareHouseRaw): Warehouse {
        return Warehouse(
            id = raw.id,
            name = raw.name,
            regionalZone = raw.regionalZone,
            latitude = raw.latitude,
            longitude = raw.longitude
        )
    }
}
package org.example.data.mapper

import org.example.data.dataholder.WareHouseRaw
import org.example.data.dataholder.WarehouseMappingResult
import org.example.domain.model.Warehouse

class WarehouseMapper {

    fun map(raw: WareHouseRaw): WarehouseMappingResult {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add(
                "Warning: Warehouse skipped - ID is missing"
            )

            return WarehouseMappingResult(
                warehouse = null,
                warnings = warnings
            )
        }

        return WarehouseMappingResult(
            warehouse = Warehouse(
                id = raw.id,
                name = raw.name,
                regionalZone = raw.regionalZone,
                latitude = raw.latitude,
                longitude = raw.longitude
            ),
            warnings = warnings
        )
    }
}
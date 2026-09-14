package org.example.data.validation

import org.example.data.dataholder.WareHouseRaw

private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0

class WarehouseValidator {

    fun validate(raw: WareHouseRaw): List<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add("Warning: Warehouse skipped - ID is missing")
        }
        if (raw.latitude == null ||
            raw.latitude < MIN_LATITUDE ||
            raw.latitude > MAX_LATITUDE
        ) {
            warnings.add("Warning: Warehouse ${raw.id} skipped - invalid latitude")
        }

        if (raw.longitude == null ||
            raw.longitude < MIN_LONGITUDE ||
            raw.longitude > MAX_LONGITUDE
        ) {
            warnings.add("Warning: Warehouse ${raw.id} skipped - invalid longitude")
        }
        return warnings

    }
}

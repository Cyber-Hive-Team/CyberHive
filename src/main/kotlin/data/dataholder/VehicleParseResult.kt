package org.example.data.dataholder

data class VehicleParseResult(
    val vehicles: List<VehicleRaw>,
    val warnings: List<String>
)
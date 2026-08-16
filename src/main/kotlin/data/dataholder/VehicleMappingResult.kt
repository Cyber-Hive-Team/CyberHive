package org.example.data.dataholder

import org.example.domain.model.Vehicle

data class VehicleMappingResult(
    val vehicle: Vehicle?,
    val warnings: List<String>
)
package org.example.domain.repository

import org.example.data.dataholder.VehicleRaw

data class VehicleRepositoryResult(
    val vehicles: List<VehicleRaw>,
    val warnings: List<String>
)
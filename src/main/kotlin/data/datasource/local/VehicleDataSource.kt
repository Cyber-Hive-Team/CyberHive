package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.VehicleRaw

interface VehicleDataSource {
    fun getVehicles(): List<RawResult<VehicleRaw>>
}
package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.datasource.local.model.VehicleLocalData

interface VehicleDataSource {
    fun getVehicles(): List<RawResult<VehicleLocalData>>
}

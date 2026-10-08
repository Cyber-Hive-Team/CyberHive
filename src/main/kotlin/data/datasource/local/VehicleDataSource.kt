package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.datasource.local.model.VehicleLocalData
import org.example.data.dataholder.VehicleRaw

interface VehicleDataSource {
    fun getVehicles(): List<RawResult<VehicleLocalData>>
    fun replaceAll(vehicles: List<VehicleRaw>)
}

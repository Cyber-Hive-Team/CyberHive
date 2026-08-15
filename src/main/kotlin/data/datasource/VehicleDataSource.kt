package org.example.data.datasource

import org.example.data.dataholder.VehicleParseResult

interface VehicleDataSource {
    fun getVehicles(): VehicleParseResult
}
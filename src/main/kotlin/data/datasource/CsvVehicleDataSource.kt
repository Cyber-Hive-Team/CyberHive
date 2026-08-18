package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataparsing.parseVehicles

class CsvVehicleDataSource(
    private val filePath: String
) : VehicleDataSource {


    override fun getVehicles(): List<RawResult<VehicleRaw>> {
        return parseVehicles(filePath)
    }
}
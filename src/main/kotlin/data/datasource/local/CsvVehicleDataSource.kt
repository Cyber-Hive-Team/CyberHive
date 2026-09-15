package org.example.data.datasource.local

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataparsing.parseVehicles
import org.example.data.datasource.VehicleDataSource

class CsvVehicleDataSource(
    private val filePath: String
) : VehicleDataSource {


    override fun getVehicles(): List<RawResult<VehicleRaw>> {
        return parseVehicles(filePath)
    }

}

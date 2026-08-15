package org.example.data.datasource

import org.example.data.dataholder.VehicleParseResult
import org.example.data.dataparsing.parseVehicles

class CsvVehicleDataSource(
    private val filePath: String
) : VehicleDataSource {


    override fun getVehicles(): VehicleParseResult {
        return parseVehicles(filePath)
    }
}
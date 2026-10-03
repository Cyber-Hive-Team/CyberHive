package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parseVehicles
import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.model.VehicleLocalData

class CsvVehicleLocalDataSource(
    private val filePath: String,
    private val warehouseDataSource: WarehouseDataSource
) : VehicleDataSource {

    override fun getVehicles(): List<RawResult<VehicleLocalData>> {

        val warehousesById = warehouseDataSource
            .getWarehouses()
            .mapNotNull { it.rawData }
            .associateBy { it.id }

        return parseVehicles(filePath).map { result ->

            val raw = result.rawData

            if (raw == null) {
                RawResult(
                    rawData = null,
                    errorMessage = result.errorMessage
                )
            } else {

                val currentWarehouse =
                    warehousesById[raw.currentHubId]

                if (currentWarehouse == null) {
                    RawResult(
                        rawData = null,
                        errorMessage =
                            "Vehicle '${raw.id}' warehouse '${raw.currentHubId}' not found."
                    )
                } else {
                    RawResult(
                        rawData = VehicleLocalData(
                            vehicleRaw = raw,
                            currentWarehouse = currentWarehouse
                        )
                    )
                }
            }
        }
    }
}

package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parseVehicles
import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.model.VehicleLocalData
import org.example.data.dataholder.VehicleRaw
import org.example.data.dataholder.WarehouseRaw

class CsvVehicleLocalDataSource(
    private val filePath: String,
    private val warehouseDataSource: WarehouseDataSource
) : VehicleDataSource {

    override fun getVehicles(): List<RawResult<VehicleLocalData>> {
        val warehousesById = loadWarehousesById()

        return parseVehicles(filePath).map { result ->
            resolveVehicle(result, warehousesById)
        }
    }

    private fun loadWarehousesById(): Map<String, WarehouseRaw> {
        return warehouseDataSource.getWarehouses()
            .mapNotNull { it.rawData }
            .associateBy { it.id }
    }

    private fun resolveVehicle(
        result: RawResult<VehicleRaw>,
        warehousesById: Map<String, WarehouseRaw>
    ): RawResult<VehicleLocalData> {
        val raw = result.rawData
            ?: return RawResult(null, result.errorMessage)

        val currentWarehouse = warehousesById[raw.currentHubId]

        return if (currentWarehouse == null) {
            RawResult(
                null,
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

    override fun replaceAll(vehicles: List<VehicleRaw>) {
        val content = buildString {
            appendLine(
                "vehicleId,currentHubId,maxCapacityKg,costPerKm"
            )

            vehicles.forEach { vehicle ->
                appendLine(
                    listOf(
                        vehicle.id,
                        vehicle.currentHubId,
                        vehicle.maxCapacityKg ?: "",
                        vehicle.costPerKm
                    ).joinToString(",")
                )
            }
        }

        replaceCsvFile(
            filePath = filePath,
            content = content
        )
    }


}

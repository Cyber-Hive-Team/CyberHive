package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parseRoutes
import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.model.RouteLocalData

class CsvRouteLocalDataSource(
    private val filePath: String,
    private val warehouseDataSource: WarehouseDataSource
) : RouteDataSource {

    override fun getRoutes(): List<RawResult<RouteLocalData>> {

        val warehousesById = warehouseDataSource
            .getWarehouses()
            .mapNotNull { it.rawData }
            .associateBy { it.id }

        return parseRoutes(filePath).map { result ->

            val raw = result.rawData

            if (raw == null) {
                RawResult(
                    rawData = null,
                    errorMessage = result.errorMessage
                )
            } else {

                val originWarehouse =
                    warehousesById[raw.originHubId]

                val destinationWarehouse =
                    warehousesById[raw.destinationHubId]

                when {
                    originWarehouse == null -> {
                        RawResult(
                            rawData = null,
                            errorMessage =
                                "Route '${raw.id}' origin warehouse '${raw.originHubId}' not found."
                        )
                    }

                    destinationWarehouse == null -> {
                        RawResult(
                            rawData = null,
                            errorMessage =
                                "Route '${raw.id}' destination warehouse '${raw.destinationHubId}' not found."
                        )
                    }

                    else -> {
                        RawResult(
                            rawData = RouteLocalData(
                                routeRaw = raw,
                                originWarehouse = originWarehouse,
                                destinationWarehouse = destinationWarehouse
                            )
                        )
                    }
                }
            }
        }
    }
}

package org.example.data.datasource.local.csv

import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parseRoutes
import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.model.RouteLocalData
import org.example.data.dataholder.RouteRaw
import org.example.data.dataholder.WarehouseRaw
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CsvRouteLocalDataSource(
    private val filePath: String,
    private val warehouseDataSource: WarehouseDataSource
) : RouteDataSource {

    override suspend fun getRoutes(): List<RawResult<RouteLocalData>> =
        withContext(Dispatchers.IO) {
            val warehousesById = loadWarehousesById()

            parseRoutes(filePath).map { result ->
                resolveRoute(result, warehousesById)
            }
        }

    private suspend fun loadWarehousesById(): Map<String, WarehouseRaw> {
        return warehouseDataSource.getWarehouses()
            .mapNotNull { it.rawData }
            .associateBy { it.id }
    }

    private fun resolveRoute(
        result: RawResult<RouteRaw>,
        warehousesById: Map<String, WarehouseRaw>
    ): RawResult<RouteLocalData> {
        val raw = result.rawData
            ?: return RawResult(null, result.errorMessage)

        val originWarehouse = warehousesById[raw.originHubId]
        val destinationWarehouse = warehousesById[raw.destinationHubId]

        return when {
            originWarehouse == null -> RawResult(
                null,
                "Route '${raw.id}' origin warehouse '${raw.originHubId}' not found."
            )

            destinationWarehouse == null -> RawResult(
                null,
                "Route '${raw.id}' destination warehouse '${raw.destinationHubId}' not found."
            )

            else -> RawResult(
                rawData = RouteLocalData(
                    routeRaw = raw,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            )
        }
    }

    override suspend fun replaceAll(routes: List<RouteRaw>) =
        withContext(Dispatchers.IO) {
        val content = buildString {
            appendLine(
                "routeId,originHubId,destinationHubId,distanceKm,typicalDelayMin"
            )

            routes.forEach { route ->
                appendLine(
                    listOf(
                        route.id,
                        route.originHubId,
                        route.destinationHubId,
                        route.distanceKm ?: "",
                        route.typicalDelayMin
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

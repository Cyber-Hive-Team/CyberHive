package org.example.data.repository

import org.example.data.dataparsing.parseRoutes
import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.WarehouseRepository

class CsvRouteRepository(
    private val filePath: String,
    private val warehouseRepository: WarehouseRepository
) : RouteRepository {

    override fun getAllRoutes(): List<Route> {
        val rawRoutes = parseRoutes(filePath)
        val warehouseMap = warehouseRepository.getAllWarehouses().associateBy { it.id }

        return rawRoutes.mapNotNull { raw ->
            val originWarehouse = warehouseMap[raw.originHubId]
            val destinationWarehouse = warehouseMap[raw.destinationHubId]

            if (originWarehouse == null || destinationWarehouse == null) {
                println("Warning: Route ${raw.id} skipped - warehouse not found.")
                return@mapNotNull null
            }

            Route(
                id = raw.id,
                distanceKm = raw.distanceKm,
                typicalDelayMin = raw.typicalDelayMin,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
}
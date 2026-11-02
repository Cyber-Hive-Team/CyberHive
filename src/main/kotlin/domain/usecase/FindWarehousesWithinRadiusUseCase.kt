package org.example.domain.usecase

import org.example.domain.algorithm.geohash.GeographicDistanceCalculator
import org.example.domain.algorithm.geohash.WarehouseSpatialIndex
import org.example.domain.model.Warehouse
import org.example.domain.model.input.FindWarehouseWithinRadiusInput
import org.example.domain.repository.WarehouseRepository

class FindWarehousesWithinRadiusUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseSpatialIndex: WarehouseSpatialIndex,
    private val geographicDistanceCalculator: GeographicDistanceCalculator
) {
    suspend operator fun invoke(
       findWarehouseWithinRadiusInput: FindWarehouseWithinRadiusInput
    ): Result<List<Warehouse>> {
        return warehouseRepository.getAllWarehouses().mapCatching { warehouses ->
            findWarehousesWithinRadius(
                warehouses = warehouses,
                latitude = findWarehouseWithinRadiusInput.latitude,
                longitude = findWarehouseWithinRadiusInput.longitude,
                radiusKm = findWarehouseWithinRadiusInput.radiusKm
            )
        }
    }

    private fun findWarehousesWithinRadius(
        warehouses: List<Warehouse>,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): List<Warehouse> {
        val spatialIndex = warehouseSpatialIndex
        spatialIndex.clear()

        warehouses.forEach(spatialIndex::add)

        return spatialIndex.findCandidatesWithinRadius(
            latitude = latitude,
            longitude = longitude,
            radiusKm = radiusKm
        ).filter { warehouse ->
            isWithinRadius(
                warehouse = warehouse,
                latitude = latitude,
                longitude = longitude,
                radiusKm = radiusKm
            )
        }
    }

    private fun isWithinRadius(
        warehouse: Warehouse,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Boolean {
        val distance = geographicDistanceCalculator.calculateDistance(
            startLatitude = latitude,
            startLongitude = longitude,
            endLatitude = warehouse.latitude,
            endLongitude = warehouse.longitude
        )

        return distance <= radiusKm
    }
}

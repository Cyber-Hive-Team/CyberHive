package org.example.domain.usecase

import org.example.domain.algorithm.search.Router
import org.example.domain.model.result.Result
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseDistance
import org.example.domain.repository.WarehouseRepository

class FindNearestWarehousesByRouteDistanceUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val router: Router
) {

    operator fun invoke(
        warehouse: Warehouse,
        limit: Int
    ): Result<List<WarehouseDistance>> {

        if (limit <= 0) {
            return Result(
                data = emptyList(),
                errorMessage = "Limit must be greater than zero."
            )
        }

        val result = warehouseRepository.getAllWarehouses()
        val nearestWarehouses = findNearestWarehouses(
            warehouse,
            result.data,
            limit
        )

        return Result(
            data = nearestWarehouses,
            errorMessage = result.errorMessage
        )
    }

    private fun findNearestWarehouses(
        warehouse: Warehouse,
        warehouses: List<Warehouse>,
        limit: Int
    ): List<WarehouseDistance> {
        return warehouses
            .asSequence()
            .filter { it.id != warehouse.id }
            .mapNotNull { findWarehouseDistance(warehouse, it) }
            .sortedBy { it.distanceKm }
            .take(limit)
            .toList()
    }

    private fun findWarehouseDistance(
        source: Warehouse,
        destination: Warehouse
    ): WarehouseDistance? {

        val result = router.findPath(
            start = source,
            destination = destination
        )

        if (result.path.isEmpty()) {
            return null
        }

        return WarehouseDistance(
            warehouse = destination,
            distanceKm = result.distanceKm
        )
    }
}

package org.example.domain.usecase

import org.example.domain.algorithm.search.Router
import org.example.domain.model.RoutingResult
import org.example.domain.repository.WarehouseRepository

class FindFewestHopsRouteUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val router: Router
) {
    operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): RoutingResult? {
        val startWarehouse = warehouseRepository.getWarehouseById(startWarehouseId)
            ?: return null

        val destinationWarehouse = warehouseRepository.getWarehouseById(destinationWarehouseId)
            ?: return null

        return router.findPath(
            start = startWarehouse,
            destination = destinationWarehouse
        )
    }
}
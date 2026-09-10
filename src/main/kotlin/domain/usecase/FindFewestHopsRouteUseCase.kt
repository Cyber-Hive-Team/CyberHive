package org.example.domain.usecase

import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.model.result.RoutingResult
import org.example.domain.repository.WarehouseRepository
import org.example.domain.model.input.FindFewestHopsRouteInput
import org.example.domain.model.exception.WarehouseNotFoundException

class FindFewestHopsRouteUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val router: BreadthFirstSearchRouter
) {

    operator fun invoke(
        input :FindFewestHopsRouteInput
    ): RoutingResult {

        val startWarehouse = warehouseRepository.getWarehouseById(input.startWarehouseId)
            ?: throw WarehouseNotFoundException(
                "Start warehouse not found with ID: ${input.startWarehouseId}"
            )

        val destinationWarehouse = warehouseRepository.getWarehouseById(input.destinationWarehouseId)
            ?: throw WarehouseNotFoundException(
                "Destination warehouse not found with ID: ${input.destinationWarehouseId}"
            )

        return router.findPath(
            start = startWarehouse,
            destination = destinationWarehouse
        )

    }

}

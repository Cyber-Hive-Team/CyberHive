package org.example.domain.usecase

import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.model.input.FindFewestHopsRouteInput
import org.example.domain.model.result.RoutingResult
import org.example.domain.repository.WarehouseRepository

class FindFewestHopsRouteUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val router: BreadthFirstSearchRouter
) {

    suspend operator fun invoke(
        input :FindFewestHopsRouteInput
    ): Result<RoutingResult> {
        return runCatching {
            val startWarehouse = warehouseRepository.getById(input.startWarehouseId)
                .getOrThrow()

            val destinationWarehouse = warehouseRepository.getById(input.destinationWarehouseId)
                .getOrThrow()

            router.findPath(start = startWarehouse, destination = destinationWarehouse)

        }
    }

}

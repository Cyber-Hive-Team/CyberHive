package org.example.domain.usecase

import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.algorithm.search.RouteWarehouseGraph
import org.example.domain.model.result.RoutingResult
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.WarehouseRepository

class FindOptimalPathUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val routeRepository: RouteRepository
) {

    operator fun invoke(
        startWarehouseId: String,
        destinationWarehouseId: String
    ): RoutingResult {

        val warehouses = warehouseRepository.getAllWarehouses().data
        val start =
            warehouses.first { warehouse ->
                warehouse.id == startWarehouseId
            }
        val destination =
            warehouses.first { warehouse ->
                warehouse.id == destinationWarehouseId
            }
        val routes = routeRepository.getAllRoutes().data
        val graph = RouteWarehouseGraph(routes)
        val router = DijkstraRouter(graph = graph, allWarehouses = warehouses)

        return router.findPath(
            start = start,
            destination = destination
        )
    }
}

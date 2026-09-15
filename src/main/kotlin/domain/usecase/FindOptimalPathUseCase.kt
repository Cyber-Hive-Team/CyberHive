package org.example.domain.usecase

import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.algorithm.search.RouteWarehouseGraph
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
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
        val start = findWarehouse(warehouses, startWarehouseId)
        val destination = findWarehouse(warehouses, destinationWarehouseId)
        val routes = routeRepository.getAllRoutes().data
        val graph = RouteWarehouseGraph(routes)
        val router = DijkstraRouter(graph = graph, allWarehouses = warehouses)

        return router.findPath(
            start = start,
            destination = destination
        )
    }

}

private fun findWarehouse(
    warehouses: List<Warehouse>,
    warehouseId: String
): Warehouse {
    return warehouses.firstOrNull {
        it.id == warehouseId
    } ?: throw WarehouseNotFoundException()

}


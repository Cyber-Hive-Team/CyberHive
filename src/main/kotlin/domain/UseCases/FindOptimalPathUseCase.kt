package org.example.domain.usecase

import org.example.domain.algorithm.search.DijkstraRouter
import org.example.domain.model.RoutingResult
import org.example.domain.model.Warehouse

class FindOptimalPathUseCase(
    private val dijkstraRouter: DijkstraRouter
) {

    operator fun invoke(
        start: Warehouse,
        destination: Warehouse
    ): RoutingResult {
        return dijkstraRouter.findPath(
            start = start,
            destination = destination
        )
    }
}
package org.example.domain.usecase

import org.example.domain.algorithm.search.Router
import org.example.domain.model.RoutingResult
import org.example.domain.model.Warehouse

class FindOptimalPathUseCase(
    private val router: Router
) {
    operator fun invoke(
        start: Warehouse,
        destination: Warehouse
    ): RoutingResult {
        return router.findPath(
            start = start,
            destination = destination
        )
    }
}
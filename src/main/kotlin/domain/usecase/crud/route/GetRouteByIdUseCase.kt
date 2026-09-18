package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository

class GetRouteByIdUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Result<Route?> {

        return runCatching {
            routeRepository.getById(routeId)
        }
    }
}

package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository
import org.example.domain.model.exception.RouteNotFoundException


class GetRouteByIdUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Result<Route?> {

        return runCatching {
            routeRepository.getById(routeId)
                ?: throw RouteNotFoundException()
        }
    }
}

package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.repository.RouteRepository

class GetRouteByIdUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Route {

        val route = routeRepository.getById(routeId)

        return route ?: throw RouteNotFoundException()
    }
}

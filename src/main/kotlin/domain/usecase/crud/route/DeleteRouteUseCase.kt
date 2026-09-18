package org.example.domain.usecase.crud.route

import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.repository.RouteRepository

class DeleteRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Boolean {

        routeRepository.getById(routeId)
            ?: throw RouteNotFoundException()

        return routeRepository.delete(routeId)
    }
}

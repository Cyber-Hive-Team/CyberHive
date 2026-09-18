package org.example.domain.usecase.crud.route

import org.example.domain.repository.RouteRepository
import org.example.domain.model.exception.RouteNotFoundException


class DeleteRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Result<Boolean> {

        return runCatching {

            routeRepository.getById(routeId)
                ?: throw RouteNotFoundException()

            routeRepository.delete(routeId)
        }
    }
}

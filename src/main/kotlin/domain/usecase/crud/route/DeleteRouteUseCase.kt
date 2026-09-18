package org.example.domain.usecase.crud.route

import org.example.domain.repository.RouteRepository

class DeleteRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Result<Boolean> {

        return runCatching {
            routeRepository.delete(routeId)
        }
    }
}

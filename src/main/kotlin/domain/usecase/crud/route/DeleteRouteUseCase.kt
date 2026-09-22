package org.example.domain.usecase.crud.route

import org.example.domain.repository.RouteRepository


class DeleteRouteUseCase(
    private val routeRepository: RouteRepository
) {

    suspend operator fun invoke(routeId: String): Result<String> {

        return runCatching {

            routeRepository.getById(routeId)
                .getOrThrow()

            routeRepository.delete(routeId).getOrThrow()

        }
    }
}

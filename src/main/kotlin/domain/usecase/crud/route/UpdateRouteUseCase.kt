package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.validator.Validator
import org.example.domain.validator.ValidationResult
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.model.exception.ValidationException

class UpdateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: Validator<Route, UpdateRouteInput>
) {

    suspend operator fun invoke(input: UpdateRouteInput): Route {

        val validationResult = routeValidator.validateUpdate(input)

        if (validationResult is ValidationResult.Failure) {
            throw ValidationException(validationResult.violations)
        }

        val currentRoute = routeRepository.getById(input.id)
            ?: throw RouteNotFoundException()

        val updatedRoute = Route(
            id = currentRoute.id,
            originWarehouse = input.originWarehouse
                ?: currentRoute.originWarehouse,
            destinationWarehouse = input.destinationWarehouse
                ?: currentRoute.destinationWarehouse,
            distanceKm = input.distanceKm
                ?: currentRoute.distanceKm,
            typicalDelayMin = input.typicalDelayMin
                ?: currentRoute.typicalDelayMin
        )

        return routeRepository.update(updatedRoute)
    }
}

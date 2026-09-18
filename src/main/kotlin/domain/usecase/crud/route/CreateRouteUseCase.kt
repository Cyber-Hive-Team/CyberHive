package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.validator.Validator
import org.example.domain.validator.ValidationResult
import org.example.domain.model.exception.ValidationException

class CreateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: Validator<Route, UpdateRouteInput>) {

    suspend operator fun invoke(route: Route): Route {

        val validationResult = routeValidator.validateCreate(route)

        if (validationResult is ValidationResult.Failure) {
            throw ValidationException(validationResult.violations)
        }

        return routeRepository.save(route)
    }
}

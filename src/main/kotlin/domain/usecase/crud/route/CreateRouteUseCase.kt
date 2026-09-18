package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.validator.Validator
import org.example.domain.validator.ValidationResult

class CreateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: Validator<Route, UpdateRouteInput>) {

    suspend operator fun invoke(route: Route): Result<Route> {

        return when (val validation = routeValidator.validateCreate(route)) {

            ValidationResult.Success -> {
                runCatching {
                    routeRepository.save(route)
                }
            }

            is ValidationResult.Failure -> {
                Result.failure(
                    IllegalArgumentException(
                        validation.violations.joinToString("; ") {
                            "${it.field}: ${it.message}"
                        }
                    )
                )
            }
        }
    }
}

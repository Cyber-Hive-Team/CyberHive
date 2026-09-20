package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator


class CreateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: Validator<Route, UpdateRouteInput>) {

    suspend operator fun invoke(route: Route): Result<Route> {

        return runCatching {
            when (
                val validation =
                    routeValidator.validateCreate(route)) {

                ValidationResult.Success -> {
                    routeRepository.save(route).getOrThrow()
                }

                is ValidationResult.Failure -> {
                        throw EntityValidationException(
                            validation.violations.joinToString("; ") {
                                "${it.field}: ${it.message}"
                            }
                        )
                }
            }
        }
    }
}

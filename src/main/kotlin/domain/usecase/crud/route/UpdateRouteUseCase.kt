package org.example.domain.usecase.crud.route

import org.example.domain.model.Route
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.validator.Validator
import org.example.domain.validator.ValidationResult
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.exception.RouteNotFoundException

class UpdateRouteUseCase(
    private val routeRepository: RouteRepository,
    private val routeValidator: Validator<Route, UpdateRouteInput>
) {

    suspend operator fun invoke(route: Route ,input: UpdateRouteInput): Result<Route> {

        return runCatching {

            routeRepository.getById(input.id)
                ?: throw RouteNotFoundException()

            when (val validation = routeValidator.validateUpdate(input)) {

                ValidationResult.Success -> {

                    val updatedRoute = Route(
                        id = route.id,
                        originWarehouse = input.originWarehouse
                            ?: route.originWarehouse,
                        destinationWarehouse = input.destinationWarehouse
                            ?: route.destinationWarehouse,
                        distanceKm = input.distanceKm
                            ?: route.distanceKm,
                        typicalDelayMin = input.typicalDelayMin
                            ?: route.typicalDelayMin
                    )

                    routeRepository.update(updatedRoute)
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

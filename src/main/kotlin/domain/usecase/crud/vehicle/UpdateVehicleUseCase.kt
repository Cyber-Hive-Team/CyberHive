package org.example.domain.usecase.crud.vehicle

import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateVehicleInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator

class UpdateVehicleUseCase(
    private val vehicleRepository: VehicleRepository,
    private val validator: Validator<Vehicle, UpdateVehicleInput>
) {

    suspend operator fun invoke(
        vehicle: Vehicle,
        input: UpdateVehicleInput
    ): Result<Vehicle> {
        return runCatching {
            vehicleRepository.getById(input.id)
                .getOrThrow()
            when (val validation = validator.validateUpdate(input)) {
                ValidationResult.Success -> {
                    vehicleRepository.update(vehicle).getOrThrow()
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

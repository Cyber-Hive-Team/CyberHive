package org.example.domain.usecase.crud.vehicle

import org.example.domain.model.Vehicle
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
        return when (val validation = validator.validateUpdate(input)) {
            ValidationResult.Success -> {
                runCatching {
                    vehicleRepository.update(vehicle)
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

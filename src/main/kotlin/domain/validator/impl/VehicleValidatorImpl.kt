package org.example.domain.validator

import org.example.domain.model.Vehicle
import org.example.domain.model.input.UpdateVehicleInput

class VehicleValidatorImpl : Validator<Vehicle, UpdateVehicleInput> {

    override fun validateId(id: String): ValidationResult {
        return toResult(
            IdValidator.validate(
                id = id,
                prefix = "TRK-",
                entityName = "Vehicle"
            )
        )
    }

    override fun validateCreate(entity: Vehicle): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(
            IdValidator.validate(
                entity.id,
                "TRK-",
                "Vehicle"
            )
        )

        if (entity.maxCapacityKg <= 0.0) {
            violations.add(FieldViolation(
                "maxCapacityKg",
                "Capacity must be greater than zero."))
        }

        if (entity.costPerKm < 0.0) {
            violations.add(FieldViolation(
                "costPerKm",
                "Cost per km cannot be negative."))
        }

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdateVehicleInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        if (validateId(input.id) is ValidationResult.Failure) {
            violations.add(FieldViolation(
                "id",
                "Invalid Vehicle ID format."))
        }

        if (input.maxCapacityKg == null && input.costPerKm == null && input.currentHub == null) {
            violations.add(FieldViolation(
                "update",
                "At least one field must be provided for update."))
        }

        input.maxCapacityKg?.let { cap ->
            if (cap <= 0.0) {
                violations.add(FieldViolation(
                    "capacityKg",
                    "Capacity must be greater than zero."))
            }
        }

        input.costPerKm?.let { cost ->
            if (cost < 0.0) {
                violations.add(FieldViolation(
                    "costPerKm",
                    "Cost per km cannot be negative."))
            }
        }

        input.currentHub?.let { hub ->
            if (hub.id.isBlank()) {
                violations.add(FieldViolation(
                    "currentHub",
                    "Current hub ID cannot be empty."))
            }
        }

        return toResult(violations)
    }

    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()){
            ValidationResult.Success
        }else{
            ValidationResult.Failure(violations)}
    }


}

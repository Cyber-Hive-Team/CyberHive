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

        violations.addAll(validateVehicleId(entity.id))
        violations.addAll(validateCapacity(entity.maxCapacityKg))
        violations.addAll(validateCost(entity.costPerKm))

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdateVehicleInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateVehicleId(input.id))
        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateCapacity(input.maxCapacityKg))
        violations.addAll(validateCost(input.costPerKm))
        violations.addAll(validateCurrentHub(input))

        return toResult(violations)
    }

    private fun validateVehicleId(id: String): List<FieldViolation> {
        return IdValidator.validate(
            id = id,
            prefix = "TRK-",
            entityName = "Vehicle"
        )
    }

    private fun validateCapacity(
        capacity: Double?
    ): List<FieldViolation> {
        if (capacity == null) {
            return emptyList()
        }

        return if (capacity <= 0.0) {
            listOf(
                FieldViolation(
                    "maxCapacityKg",
                    "Capacity must be greater than zero."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateCost(
        cost: Double?
    ): List<FieldViolation> {
        if (cost == null) {
            return emptyList()
        }

        return if (cost < 0.0) {
            listOf(
                FieldViolation(
                    "costPerKm",
                    "Cost per km cannot be negative."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateUpdateFields(
        input: UpdateVehicleInput
    ): List<FieldViolation> {
        return if (hasNoUpdates(input)) {
            listOf(
                FieldViolation(
                    "update",
                    "At least one field must be provided for update."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun hasNoUpdates(input: UpdateVehicleInput): Boolean {
        return input.maxCapacityKg == null &&
                input.costPerKm == null &&
                input.currentHub == null
    }

    private fun validateCurrentHub(
        input: UpdateVehicleInput
    ): List<FieldViolation> {
        val hub = input.currentHub ?: return emptyList()

        return if (hub.id.isBlank()) {
            listOf(
                FieldViolation(
                    "currentHub",
                    "Current hub ID cannot be empty."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()){
            ValidationResult.Success
        }else{
            ValidationResult.Failure(violations)}
    }

}

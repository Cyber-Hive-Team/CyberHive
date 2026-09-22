package org.example.domain.validator.impl

import org.example.domain.model.Vehicle
import org.example.domain.model.exception.DomainException
import org.example.domain.model.input.UpdateVehicleInput
import org.example.domain.validator.FieldError
import org.example.domain.validator.FieldViolation
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.example.domain.validator.toResult

class VehicleValidatorImpl : Validator<Vehicle, UpdateVehicleInput> {

    override fun validateCreate(entity: Vehicle): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateCapacity(entity.maxCapacityKg))
        violations.addAll(validateCost(entity.costPerKm))

        return violations.toResult()
    }

    override fun validateUpdate(input: UpdateVehicleInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateCapacity(input.maxCapacityKg))
        violations.addAll(validateCost(input.costPerKm))
        violations.addAll(validateCurrentHub(input))

        return violations.toResult()
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
                    FieldError.InvalidCapacity,
                    DomainException.INVALID_VEHICLE_CAPACITY
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
                    FieldError.InvalidCostPerKm,
                    DomainException.INVALID_COST_PER_KM
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
                    FieldError.NoUpdateFields,
                    DomainException.NO_UPDATE_FIELDS
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
                    FieldError.InvalidCurrentHub,
                    DomainException.INVALID_CURRENT_HUB
                )
            )
        } else {
            emptyList()
        }
    }

}

package org.example.domain.validator

import org.example.domain.model.Package
import org.example.domain.model.input.UpdatePackageInput

class PackageValidatorImpl : Validator<Package, UpdatePackageInput> {

    override fun validateId(id: String): ValidationResult {
        return toResult(
            IdValidator.validate(
                id = id,
                prefix = "PKG-",
                entityName = "Package"
            )
        )
    }

    override fun validateCreate(entity: Package): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(
            IdValidator.validate(
                entity.id,
                "PKG-",
                "Package"
            )
        )

        if (entity.weight <= 0.0) {
            violations.add(FieldViolation(
                "weight",
                "Weight must be strictly greater than zero."))
        }

        if (entity.baseRate < 0.0) {
            violations.add(FieldViolation(
                "baseRate",
                "Base rate cannot be negative."))
        }

        if (entity.originWarehouse.id.isBlank()) {
            violations.add(FieldViolation(
                "originWarehouse",
                "Origin warehouse ID cannot be empty."))
        }
        if (entity.destinationWarehouse.id.isBlank()) {
            violations.add(FieldViolation(
                "destinationWarehouse",
                "Destination warehouse ID cannot be empty."))
        }

        if (entity.originWarehouse.id.isNotBlank() && entity.originWarehouse.id == entity.destinationWarehouse.id) {
            violations.add(FieldViolation(
                "destinationWarehouse",
                "Origin and Destination warehouses cannot be the same."))
        }

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdatePackageInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        if (validateId(input.id) is ValidationResult.Failure) {
            violations.add(FieldViolation(
                "id",
                "Invalid Package ID format."))
        }

        if (input.weight == null && input.priority == null &&
            input.originWarehouse == null && input.destinationWarehouse == null && input.baseRate == null) {
            violations.add(FieldViolation(
                "update",
                "At least one field must be provided for update."))
        }

        input.weight?.let { weightValue ->
            if (weightValue <= 0.0) {
                violations.add(FieldViolation(
                    "weight",
                    "Weight must be strictly greater than zero."))
            }
        }

        input.baseRate?.let { rate ->
            if (rate < 0.0) {
                violations.add(FieldViolation(
                    "baseRate",
                    "Base rate cannot be negative."))
            }
        }

        input.originWarehouse?.let { origin ->
            if (origin.id.isBlank()) {
                violations.add(FieldViolation(
                    "originWarehouse",
                    "Origin warehouse ID cannot be empty."))
            }
        }

        input.destinationWarehouse?.let { destination ->
            if (destination.id.isBlank()) {
                violations.add(FieldViolation(
                    "destinationWarehouse",
                    "Destination warehouse ID cannot be empty."))
            }
        }

        return toResult(violations)
    }

    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()) {
            ValidationResult.Success
        }else {
            ValidationResult.Failure(violations)
        }
    }
}

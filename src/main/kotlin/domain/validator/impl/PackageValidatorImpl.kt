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

        violations.addAll(validateCreateId(entity.id))
        violations.addAll(validateWeight(entity.weight))
        violations.addAll(validateBaseRate(entity.baseRate))
        violations.addAll(
            validateWarehouses(
                entity.originWarehouse.id,
                entity.destinationWarehouse.id
            )
        )

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdatePackageInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateUpdateId(input.id))
        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateWeight(input.weight))
        violations.addAll(validateBaseRate(input.baseRate))
        violations.addAll(validateOriginWarehouse(input))
        violations.addAll(validateDestinationWarehouse(input))
        violations.addAll(
            validateDifferentWarehouses(
                input.originWarehouse?.id,
                input.destinationWarehouse?.id
            )
        )

        return toResult(violations)
    }

    private fun validateCreateId(id: String): List<FieldViolation> {
        return IdValidator.validate(
            id = id,
            prefix = "PKG-",
            entityName = "Package"
        )
    }

    private fun validateUpdateId(id: String): List<FieldViolation> {
        return IdValidator.validate(
            id = id,
            prefix = "PKG-",
            entityName = "Package"
        )
    }

    private fun validateWeight(weight: Double?): List<FieldViolation> {
        if (weight == null) {
            return emptyList()
        }

        return if (weight <= 0.0) {
            listOf(
                FieldViolation(
                    "weight",
                    "Weight must be strictly greater than zero."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateBaseRate(rate: Double?): List<FieldViolation> {
        if (rate == null) {
            return emptyList()
        }

        return if (rate < 0.0) {
            listOf(
                FieldViolation(
                    "baseRate",
                    "Base rate cannot be negative."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateWarehouses(
        originId: String,
        destinationId: String
    ): List<FieldViolation> {
        val violations = mutableListOf<FieldViolation>()

        if (originId.isBlank()) {
            violations.add(
                FieldViolation(
                    "originWarehouse",
                    "Origin warehouse ID cannot be empty."
                )
            )
        }

        if (destinationId.isBlank()) {
            violations.add(
                FieldViolation(
                    "destinationWarehouse",
                    "Destination warehouse ID cannot be empty."
                )
            )
        }

        if (originId.isNotBlank() && originId == destinationId) {
            violations.add(
                FieldViolation(
                    "destinationWarehouse",
                    "Origin and Destination warehouses cannot be the same."
                )
            )
        }

        return violations
    }

    private fun validateUpdateFields(
        input: UpdatePackageInput
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

    private fun hasNoUpdates(input: UpdatePackageInput): Boolean {
        return input.weight == null &&
                input.priority == null &&
                input.originWarehouse == null &&
                input.destinationWarehouse == null &&
                input.baseRate == null
    }

    private fun validateOriginWarehouse(
        input: UpdatePackageInput
    ): List<FieldViolation> {
        val origin = input.originWarehouse ?: return emptyList()

        return if (origin.id.isBlank()) {
            listOf(
                FieldViolation(
                    "originWarehouse",
                    "Origin warehouse ID cannot be empty."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateDestinationWarehouse(
        input: UpdatePackageInput
    ): List<FieldViolation> {
        val destination = input.destinationWarehouse ?: return emptyList()

        return if (destination.id.isBlank()) {
            listOf(
                FieldViolation(
                    "destinationWarehouse",
                    "Destination warehouse ID cannot be empty."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateDifferentWarehouses(
        originId: String?,
        destinationId: String?
    ): List<FieldViolation> {

        val violations = mutableListOf<FieldViolation>()

        if (originId != null && destinationId != null) {
            if (originId.isNotBlank() &&
                destinationId.isNotBlank() &&
                originId == destinationId
            ) {
                violations.add(
                    FieldViolation(
                        "destinationWarehouse",
                        "Origin and Destination warehouses cannot be the same."
                    )
                )
            }
        }

        return violations
    }

    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()) {
            ValidationResult.Success
        }else {
            ValidationResult.Failure(violations)
        }
    }

}

package org.example.domain.validator

import org.example.domain.model.Package
import org.example.domain.model.exception.DomainException
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.result.FieldViolation
import org.example.domain.validator.result.ValidationResult
import org.example.domain.validator.result.toResult

class PackageValidator : Validator<Package, UpdatePackageInput> {

    override fun validateCreate(entity: Package): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateBaseRate(entity.baseRate))
        violations.addAll(
            validateWarehouses(
                entity.originWarehouse.id,
                entity.destinationWarehouse.id
            )
        )

        return violations.toResult()
    }

    override fun validateUpdate(input: UpdatePackageInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateWeight(input.weight))
        violations.addAll(validateOriginWarehouse(input))
        violations.addAll(validateDestinationWarehouse(input))
        violations.addAll(
            validateDifferentWarehouses(
                input.originWarehouse.id,
                input.destinationWarehouse.id
            )
        )

        return violations.toResult()
    }

    private fun validateWeight(weight: Double?): List<FieldViolation> {
        if (weight == null) {
            return emptyList()
        }

        return if (weight <= 0.0) {
            listOf(
                FieldViolation(
                    FieldError.InvalidWeight,
                    DomainException.Companion.INVALID_PACKAGE_WEIGHT
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
                    FieldError.InvalidBaseRate,
                    DomainException.Companion.INVALID_BASE_RATE
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
                    FieldError.InvalidOriginWarehouse,
                    DomainException.Companion.INVALID_ORIGIN_WAREHOUSE
                )
            )
        }

        if (destinationId.isBlank()) {
            violations.add(
                FieldViolation(
                    FieldError.InvalidDestinationWarehouse,
                    DomainException.Companion.INVALID_DESTINATION_WAREHOUSE
                )
            )
        }

        if (originId.isNotBlank() && originId == destinationId) {
            violations.add(
                FieldViolation(
                    FieldError.SameWarehouse,
                    DomainException.Companion.SAME_WAREHOUSE
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
                    FieldError.NoUpdateFields,
                    DomainException.Companion.NO_UPDATE_FIELDS
                )
            )
        } else {
            emptyList()
        }
    }

    private fun hasNoUpdates(input: UpdatePackageInput): Boolean {
        return input.weight == null &&
                input.priority == null
    }

    private fun validateOriginWarehouse(
        input: UpdatePackageInput
    ): List<FieldViolation> {
        val origin = input.originWarehouse

        return if (origin.id.isBlank()) {
            listOf(
                FieldViolation(
                    FieldError.InvalidOriginWarehouse,
                    DomainException.Companion.INVALID_ORIGIN_WAREHOUSE
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateDestinationWarehouse(
        input: UpdatePackageInput
    ): List<FieldViolation> {
        val destination = input.destinationWarehouse

        return if (destination.id.isBlank()) {
            listOf(
                FieldViolation(
                    FieldError.InvalidDestinationWarehouse,
                    DomainException.Companion.INVALID_DESTINATION_WAREHOUSE
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
                        FieldError.SameWarehouse,
                        DomainException.Companion.SAME_WAREHOUSE
                    )
                )
            }
        }

        return violations
    }

}
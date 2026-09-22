package org.example.domain.validator.impl

import org.example.domain.model.Package
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.model.exception.DomainException
import org.example.domain.validator.FieldViolation
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.example.domain.validator.toResult

class PackageValidatorImpl : Validator<Package, UpdatePackageInput> {

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
                    "weight",
                    DomainException.INVALID_PACKAGE_WEIGHT
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
                    DomainException.INVALID_BASE_RATE
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
                    DomainException.INVALID_ORIGIN_WAREHOUSE
                )
            )
        }

        if (destinationId.isBlank()) {
            violations.add(
                FieldViolation(
                    "destinationWarehouse",
                    DomainException.INVALID_DESTINATION_WAREHOUSE
                )
            )
        }

        if (originId.isNotBlank() && originId == destinationId) {
            violations.add(
                FieldViolation(
                    "destinationWarehouse",
                    DomainException.SAME_WAREHOUSE
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
                    DomainException.NO_UPDATE_FIELDS
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
                    "originWarehouse",
                    DomainException.INVALID_ORIGIN_WAREHOUSE
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
                    "destinationWarehouse",
                    DomainException.INVALID_DESTINATION_WAREHOUSE
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
                        DomainException.SAME_WAREHOUSE
                    )
                )
            }
        }

        return violations
    }

}

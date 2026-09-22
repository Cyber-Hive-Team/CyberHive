package org.example.domain.validator.impl

import org.example.domain.model.Warehouse
import org.example.domain.model.exception.DomainException
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.validator.FieldError
import org.example.domain.validator.FieldViolation
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.example.domain.validator.toResult

private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0

class WarehouseValidatorImpl : Validator<Warehouse, UpdateWarehouseInput> {

    override fun validateCreate(entity: Warehouse): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateName(entity.name))
        violations.addAll(validateLatitude(entity.latitude))
        violations.addAll(validateLongitude(entity.longitude))

        return violations.toResult()
    }

    override fun validateUpdate(input: UpdateWarehouseInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateName(input.name))
        violations.addAll(validateLatitude(input.latitude))
        violations.addAll(validateLongitude(input.longitude))

        return violations.toResult()
    }

    private fun validateName(
        name: String?
    ): List<FieldViolation> {
        if (name == null) {
            return emptyList()
        }

        return if (name.isBlank()) {
            listOf(
                FieldViolation(
                    FieldError.InvalidWarehouseName,
                    DomainException.INVALID_WAREHOUSE_NAME
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateLatitude(
        latitude: Double?
    ): List<FieldViolation> {
        if (latitude == null) {
            return emptyList()
        }

        return if (latitude !in MIN_LATITUDE..MAX_LATITUDE) {
            listOf(
                FieldViolation(
                    FieldError.InvalidLatitude,
                    DomainException.INVALID_LATITUDE
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateLongitude(
        longitude: Double?
    ): List<FieldViolation> {
        if (longitude == null) {
            return emptyList()
        }

        return if (longitude !in MIN_LONGITUDE..MAX_LONGITUDE) {
            listOf(
                FieldViolation(
                    FieldError.InvalidLongitude,
                    DomainException.INVALID_LONGITUDE
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateUpdateFields(
        input: UpdateWarehouseInput
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

    private fun hasNoUpdates(
        input: UpdateWarehouseInput
    ): Boolean {
        return input.name == null &&
                input.regionalZone == null &&
                input.latitude == null &&
                input.longitude == null
    }

}

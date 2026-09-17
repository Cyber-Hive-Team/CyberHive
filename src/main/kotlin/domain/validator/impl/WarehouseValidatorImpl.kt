package org.example.domain.validator

import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdateWarehouseInput

private const val MIN_LATITUDE = -90.0
private const val MAX_LATITUDE = 90.0
private const val MIN_LONGITUDE = -180.0
private const val MAX_LONGITUDE = 180.0

class WarehouseValidatorImpl : Validator<Warehouse, UpdateWarehouseInput> {

    override fun validateId(id: String): ValidationResult {
        return toResult(
            IdValidator.validate(
                id = id,
                prefix = "WH-",
                entityName = "Warehouse"
            )
        )
    }

    override fun validateCreate(entity: Warehouse): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateWarehouseId(entity.id))
        violations.addAll(validateName(entity.name))
        violations.addAll(validateLatitude(entity.latitude))
        violations.addAll(validateLongitude(entity.longitude))

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdateWarehouseInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateWarehouseId(input.id))
        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateName(input.name))
        violations.addAll(validateLatitude(input.latitude))
        violations.addAll(validateLongitude(input.longitude))

        return toResult(violations)
    }

    private fun validateWarehouseId(
        id: String
    ): List<FieldViolation> {
        return IdValidator.validate(
            id = id,
            prefix = "WH-",
            entityName = "Warehouse"
        )
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
                    "name",
                    "Warehouse name cannot be empty."
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
                    "latitude",
                    "Latitude must be between -90 and 90."
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
                    "longitude",
                    "Longitude must be between -180 and 180."
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
                    "update",
                    "At least one field must be provided for update."
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



    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()){
            ValidationResult.Success
        }else {
            ValidationResult.Failure(violations)
        }
    }

}

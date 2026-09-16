package org.example.domain.validator

import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdateWarehouseInput

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

        violations.addAll(
            IdValidator.validate(
                entity.id,
                "WH-",
                "Warehouse"
            )
        )

        if (entity.name.isBlank()) {
            violations.add(FieldViolation(
                "name",
                "Warehouse name cannot be empty."))
        }

        if (entity.latitude !in -90.0..90.0) {
            violations.add(FieldViolation(
                "latitude",
                "Latitude must be between -90 and 90."))
        }

        if (entity.longitude !in -180.0..180.0) {
            violations.add(FieldViolation(
                "longitude",
                "Longitude must be between -180 and 180."))
        }

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdateWarehouseInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        if (validateId(input.id) is ValidationResult.Failure) {
            violations.add(FieldViolation(
                "id",
                "Invalid Warehouse ID format."))
        }

        if (input.name == null && input.regionalZone == null && input.latitude == null && input.longitude == null) {
            violations.add(FieldViolation(
                "update",
                "At least one field must be provided for update."))
        }

        input.name?.let { nameValue ->
            if (nameValue.isBlank()) {
                violations.add(FieldViolation(
                    "name",
                    "Warehouse name cannot be empty."))
            }
        }

        input.latitude?.let { lat ->
            if (lat !in -90.0..90.0) {
                violations.add(FieldViolation(
                    "latitude",
                    "Latitude must be between -90 and 90."))
            }
        }

        input.longitude?.let { lon ->
            if (lon !in -180.0..180.0) {
                violations.add(FieldViolation(
                    "longitude",
                    "Longitude must be between -180 and 180."))
            }
        }

        return toResult(violations)
    }

    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()){
            ValidationResult.Success
        }else {
            ValidationResult.Failure(violations)
        }
    }

}

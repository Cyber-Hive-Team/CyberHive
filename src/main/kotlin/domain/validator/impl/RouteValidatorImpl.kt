package org.example.domain.validator

import org.example.domain.model.Route
import org.example.domain.model.input.UpdateRouteInput

class RouteValidatorImpl : Validator<Route, UpdateRouteInput> {

    override fun validateId(id: String): ValidationResult {
        return toResult(
            IdValidator.validate(
                id = id,
                prefix = "RT-",
                entityName = "Route"
            )
        )
    }

    override fun validateCreate(entity: Route): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(
            IdValidator.validate(
                entity.id,
                "RT-",
                "Route"
            )
        )

        if (entity.distanceKm <= 0.0) {
            violations.add(FieldViolation(
                "distanceKm",
                "Distance must be greater than zero."))
        }

        if (entity.typicalDelayMin < 0) {
            violations.add(FieldViolation
                ("typicalDelayMin",
                "Typical delay cannot be negative."))
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

        return toResult(violations)
    }

    override fun validateUpdate(input: UpdateRouteInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        if (validateId(input.id) is ValidationResult.Failure) {
            violations.add(FieldViolation(
                "id",
                "Invalid Route ID format."))
        }

        if (input.distanceKm == null && input.typicalDelayMin == null &&
            input.originWarehouse == null && input.destinationWarehouse== null) {
            violations.add(FieldViolation(
                "update",
                "At least one field must be provided for update."))
        }

        input.distanceKm?.let { dist ->
            if (dist <= 0.0) {
                violations.add(FieldViolation(
                    "distanceKm",
                    "Distance must be greater than zero."))
            }
        }

        input.typicalDelayMin?.let { delay ->
            if (delay < 0) {
                violations.add(FieldViolation(
                    "typicalDelayMin",
                    "Typical delay cannot be negative."))
            }
        }

        input.originWarehouse?.let { origin ->
            if (origin.id.isBlank()) {
                violations.add(FieldViolation(
                    "originWarehouseId",
                    "Origin warehouse ID cannot be empty."))
            }
        }

        input.destinationWarehouse?.let { destination ->
            if (destination.id.isBlank()) {
                violations.add(FieldViolation(
                    "destinationWarehouseId",
                    "Destination warehouse ID cannot be empty."))
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

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

        violations.addAll(validateRouteId(entity.id))
        violations.addAll(validateDistance(entity.distanceKm))
        violations.addAll(validateDelay(entity.typicalDelayMin))
        violations.addAll(
            validateWarehouses(
                entity.originWarehouse.id,
                entity.destinationWarehouse.id
            )
        )

        return  toResult(violations)
    }

    override fun validateUpdate(input: UpdateRouteInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateRouteId(input.id))
        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateDistance(input.distanceKm))
        violations.addAll(validateDelay(input.typicalDelayMin))
        violations.addAll(validateOriginWarehouse(input))
        violations.addAll(validateDestinationWarehouse(input))

        return toResult(violations)
    }

    private fun validateRouteId(id: String): List<FieldViolation> {
        return IdValidator.validate(
            id = id,
            prefix = "RT-",
            entityName = "Route"
        )
    }

    private fun validateDistance(
        distance: Double?
    ): List<FieldViolation> {
        if (distance == null) {
            return emptyList()
        }

        return if (distance <= 0.0) {
            listOf(
                FieldViolation(
                    "distanceKm",
                    "Distance must be greater than zero."
                )
            )
        } else {
            emptyList()
        }
    }

    private fun validateDelay(
        delay: Int?
    ): List<FieldViolation> {
        if (delay == null) {
            return emptyList()
        }

        return if (delay < 0) {
            listOf(
                FieldViolation(
                    "typicalDelayMin",
                    "Typical delay cannot be negative."
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

        return violations
    }

    private fun validateUpdateFields(
        input: UpdateRouteInput
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

    private fun hasNoUpdates(input: UpdateRouteInput): Boolean {
        return input.distanceKm == null &&
                input.typicalDelayMin == null &&
                input.originWarehouse == null &&
                input.destinationWarehouse == null
    }

    private fun validateOriginWarehouse(
        input: UpdateRouteInput
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
        input: UpdateRouteInput
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

    private fun toResult(violations: List<FieldViolation>): ValidationResult {
        return if (violations.isEmpty()){
            ValidationResult.Success
        }else {
            ValidationResult.Failure(violations)
        }
    }

}

package org.example.domain.validator.impl

import org.example.domain.model.Route
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.validator.FieldViolation
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.example.domain.validator.toResult
import org.example.domain.model.exception.DomainException

class RouteValidatorImpl : Validator<Route, UpdateRouteInput> {

    override fun validateCreate(entity: Route): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateDistance(entity.distanceKm))
        violations.addAll(validateDelay(entity.typicalDelayMin))
        violations.addAll(
            validateWarehouses(
                entity.originWarehouse.id,
                entity.destinationWarehouse.id
            )
        )

        return  violations.toResult()
    }

    override fun validateUpdate(input: UpdateRouteInput): ValidationResult {
        val violations = mutableListOf<FieldViolation>()

        violations.addAll(validateUpdateFields(input))
        violations.addAll(validateDistance(input.distanceKm))
        violations.addAll(validateDelay(input.typicalDelayMin))
        violations.addAll(validateOriginWarehouse(input))
        violations.addAll(validateDestinationWarehouse(input))

        return violations.toResult()
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
                    DomainException.INVALID_DISTANCE
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
                    DomainException.INVALID_DELAY
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

        return violations
    }

    private fun validateUpdateFields(
        input: UpdateRouteInput
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
                    DomainException.INVALID_ORIGIN_WAREHOUSE
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
                    DomainException.INVALID_DESTINATION_WAREHOUSE
                )
            )
        } else {
            emptyList()
        }
    }

}

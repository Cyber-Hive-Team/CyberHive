package org.example.test.domain.validator.impl

import org.example.domain.model.exception.DomainException
import org.example.domain.model.input.UpdateVehicleInput
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.result.FieldViolation
import org.example.domain.validator.result.ValidationResult
import org.example.domain.validator.VehicleValidator
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VehicleValidatorTest {

    private val factory = TestDataFactory()
    private val validator = VehicleValidator()

    @Test
    fun `accepts valid vehicle creation`() {
        // Given
        val vehicle = factory.createVehicle()

        // When
        val result = validator.validateCreate(vehicle)

        // Then
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `rejects zero capacity on create`() {
        // Given
        val vehicle = factory.createVehicle(maxCapacityKg = 0.0)

        // When
        val result = validator.validateCreate(vehicle)

        // Then
        assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidCapacity,
                        DomainException.INVALID_VEHICLE_CAPACITY
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `rejects negative cost on create`() {
        // Given
        val vehicle = factory.createVehicle(costPerKm = -1.0)

        // When
        val result = validator.validateCreate(vehicle)

        // Then
        assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidCostPerKm,
                        DomainException.INVALID_COST_PER_KM
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `accepts capacity update`() {
        // Given
        val input = UpdateVehicleInput(
            id = "TRK-0001",
            maxCapacityKg = 120.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertEquals(ValidationResult.Success, result)
    }

    @Test
    fun `rejects update with no fields`() {
        // Given
        val input = UpdateVehicleInput(id = "TRK-0001")

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.NoUpdateFields,
                        DomainException.NO_UPDATE_FIELDS
                    )
                )
            ),
            result
        )
    }

    @Test
    fun `reports multiple update violations`() {
        // Given
        val input = UpdateVehicleInput(
            id = "TRK-0001",
            maxCapacityKg = -1.0,
            costPerKm = -1.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertEquals(
            ValidationResult.Failure(
                listOf(
                    FieldViolation(
                        FieldError.InvalidCapacity,
                        DomainException.INVALID_VEHICLE_CAPACITY
                    ),
                    FieldViolation(
                        FieldError.InvalidCostPerKm,
                        DomainException.INVALID_COST_PER_KM
                    )
                )
            ),
            result
        )
    }
}

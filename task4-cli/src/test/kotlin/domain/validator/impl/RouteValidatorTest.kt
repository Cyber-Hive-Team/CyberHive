package org.example.test.domain.validator.impl

import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.validator.RouteValidator
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.result.ValidationResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RouteValidatorTest {

    private val validator = RouteValidator()

    private val warehouse1 = Warehouse(
        id = "WH-001",
        name = "Warehouse 1",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.5
    )

    private val warehouse2 = Warehouse(
        id = "WH-002",
        name = "Warehouse 2",
        regionalZone = RegionalZone.SOUTH,
        latitude = 31.4,
        longitude = 34.6
    )

    @Test
    fun `valid route should return success`() {

        // Given
        val route = Route(
            id = "RT-00001",
            distanceKm = 100.0,
            typicalDelayMin = 10,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        // When
        val result = validator.validateCreate(route)

        // Then
        assertEquals(
            ValidationResult.Success,
            result
        )
    }

    @Test
    fun `route with zero distance should return invalid distance`() {

        // Given
        val route = Route(
            id = "RT-00001",
            distanceKm = 0.0,
            typicalDelayMin = 10,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        // When
        val result = validator.validateCreate(route)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations.first().field
        )
    }

    @Test
    fun `route with negative distance should return invalid distance`() {

        // Given
        val route = Route(
            id = "RT-00001",
            distanceKm = -50.0,
            typicalDelayMin = 10,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        // When
        val result = validator.validateCreate(route)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations.first().field
        )
    }

    @Test
    fun `route with negative delay should return invalid delay`() {

        // Given
        val route = Route(
            id = "RT-00001",
            distanceKm = 100.0,
            typicalDelayMin = -1,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        // When
        val result = validator.validateCreate(route)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            FieldError.InvalidDelay,
            failure.violations.first().field
        )
    }

    @Test
    fun `route with zero delay should return success`() {

        // Given
        val route = Route(
            id = "RT-00001",
            distanceKm = 100.0,
            typicalDelayMin = 0,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        // When
        val result = validator.validateCreate(route)

        // Then
        assertEquals(
            ValidationResult.Success,
            result
        )
    }

    @Test
    fun `valid update should return success`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001",
            distanceKm = 150.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertEquals(
            ValidationResult.Success,
            result
        )
    }

    @Test
    fun `update with no fields should return no update fields`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001"
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            FieldError.NoUpdateFields,
            failure.violations.first().field
        )
    }

    @Test
    fun `update with invalid distance should return invalid distance`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001",
            distanceKm = -10.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations.first().field
        )
    }

    @Test
    fun `update with invalid delay should return invalid delay`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001",
            typicalDelayMin = -5
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            FieldError.InvalidDelay,
            failure.violations.first().field
        )
    }

    @Test
    fun `update with valid origin warehouse should return success`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001",
            originWarehouse = warehouse2
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertEquals(
            ValidationResult.Success,
            result
        )
    }

    @Test
    fun `update with valid destination warehouse should return success`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001",
            destinationWarehouse = warehouse2
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertEquals(
            ValidationResult.Success,
            result
        )
    }

    @Test
    fun `update with multiple invalid fields should return multiple violations`() {

        // Given
        val input = UpdateRouteInput(
            id = "RT-00001",
            distanceKm = -10.0,
            typicalDelayMin = -5
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        val failure = result as ValidationResult.Failure

        assertEquals(
            listOf(
                FieldError.InvalidDistance,
                FieldError.InvalidDelay
            ),
            failure.violations.map { it.field }
        )
    }
}

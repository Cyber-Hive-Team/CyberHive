package org.example.test.domain.validator.impl

import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.validator.result.*
import org.example.domain.validator.RouteValidator
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RouteValidatorImplTest {

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
        val route = Route(
            id = "RT-00001",
            distanceKm = 100.0,
            typicalDelayMin = 10,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        val result = validator.validateCreate(route)

        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun `route with zero distance should return invalid distance`() {
        val route = Route(
            id = "RT-00001",
            distanceKm = 0.0,
            typicalDelayMin = 10,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        val result = validator.validateCreate(route)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations.first().field
        )
    }

    @Test
    fun `route with negative distance should return invalid distance`() {
        val route = Route(
            id = "RT-00001",
            distanceKm = -50.0,
            typicalDelayMin = 10,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        val result = validator.validateCreate(route)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations.first().field
        )
    }

    @Test
    fun `route with negative delay should return invalid delay`() {
        val route = Route(
            id = "RT-00001",
            distanceKm = 100.0,
            typicalDelayMin = -1,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        val result = validator.validateCreate(route)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            FieldError.InvalidDelay,
            failure.violations.first().field
        )
    }

    @Test
    fun `route with zero delay should return success`() {
        val route = Route(
            id = "RT-00001",
            distanceKm = 100.0,
            typicalDelayMin = 0,
            originWarehouse = warehouse1,
            destinationWarehouse = warehouse2
        )

        val result = validator.validateCreate(route)

        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun `valid update should return success`() {
        val input = UpdateRouteInput(
            id = "RT-00001",
            distanceKm = 150.0
        )

        val result = validator.validateUpdate(input)

        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun `update with no fields should return no update fields`() {
        val input = UpdateRouteInput(
            id = "RT-00001"
        )

        val result = validator.validateUpdate(input)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            FieldError.NoUpdateFields,
            failure.violations.first().field
        )
    }

    @Test
    fun `update with invalid distance should return invalid distance`() {
        val input = UpdateRouteInput(
            id = "RT-00001",
            distanceKm = -10.0
        )

        val result = validator.validateUpdate(input)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations.first().field
        )
    }

    @Test
    fun `update with invalid delay should return invalid delay`() {
        val input = UpdateRouteInput(
            id = "RT-00001",
            typicalDelayMin = -5
        )

        val result = validator.validateUpdate(input)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            FieldError.InvalidDelay,
            failure.violations.first().field
        )
    }

    @Test
    fun `update with valid origin warehouse should return success`() {
        val input = UpdateRouteInput(
            id = "RT-00001",
            originWarehouse = warehouse2
        )

        val result = validator.validateUpdate(input)

        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun `update with valid destination warehouse should return success`() {
        val input = UpdateRouteInput(
            id = "RT-00001",
            destinationWarehouse = warehouse2
        )

        val result = validator.validateUpdate(input)

        assertIs<ValidationResult.Success>(result)
    }

    @Test
    fun `update with multiple invalid fields should return multiple violations`() {
        val input = UpdateRouteInput(
            id = "RT-00001",
            distanceKm = -10.0,
            typicalDelayMin = -5
        )

        val result = validator.validateUpdate(input)

        val failure = assertIs<ValidationResult.Failure>(result)

        assertEquals(
            2,
            failure.violations.size
        )

        assertEquals(
            FieldError.InvalidDistance,
            failure.violations[0].field
        )

        assertEquals(
            FieldError.InvalidDelay,
            failure.violations[1].field
        )
    }
}

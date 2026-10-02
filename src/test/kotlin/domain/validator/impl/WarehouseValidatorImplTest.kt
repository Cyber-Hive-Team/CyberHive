package org.example.domain.validator.impl

import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.validator.*
import org.example.domain.validator.result.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WarehouseValidatorImplTest {

    private val validator = WarehouseValidator()

    @Test
    fun `valid warehouse returns success`() {
        // Given
        val warehouse = warehouse()

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertEquals(
            ValidationResult.Success,
            result
        )
    }

    @Test
    fun `blank name returns name violation`() {
        // Given
        val warehouse = warehouse(name = "   ")

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidWarehouseName
        )
    }

    @Test
    fun `latitude below minimum returns latitude violation`() {
        // Given
        val warehouse = warehouse(latitude = -90.1)

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidLatitude
        )
    }

    @Test
    fun `latitude above maximum returns latitude violation`() {
        // Given
        val warehouse = warehouse(latitude = 90.1)

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidLatitude
        )
    }

    @Test
    fun `longitude below minimum returns longitude violation`() {
        // Given
        val warehouse = warehouse(longitude = -180.1)

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidLongitude
        )
    }

    @Test
    fun `longitude above maximum returns longitude violation`() {
        // Given
        val warehouse = warehouse(longitude = 180.1)

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidLongitude
        )
    }

    @Test
    fun `latitude boundaries are accepted`() {
        // Given
        val lower = warehouse(latitude = -90.0)
        val upper = warehouse(latitude = 90.0)

        // When
        val lowerResult = validator.validateCreate(lower)
        val upperResult = validator.validateCreate(upper)

        // Then
        assertEquals(ValidationResult.Success, lowerResult)
        assertEquals(ValidationResult.Success, upperResult)
    }

    @Test
    fun `longitude boundaries are accepted`() {
        // Given
        val lower = warehouse(longitude = -180.0)
        val upper = warehouse(longitude = 180.0)

        // When
        val lowerResult = validator.validateCreate(lower)
        val upperResult = validator.validateCreate(upper)

        // Then
        assertEquals(ValidationResult.Success, lowerResult)
        assertEquals(ValidationResult.Success, upperResult)
    }

    @Test
    fun `multiple invalid fields return all violations`() {
        // Given
        val warehouse = warehouse(
            name = "",
            latitude = -91.0,
            longitude = 181.0
        )

        // When
        val result = validator.validateCreate(warehouse)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidWarehouseName,
            FieldError.InvalidLatitude,
            FieldError.InvalidLongitude
        )
    }

    // -------------------------
    // Update validation
    // -------------------------

    @Test
    fun `update without fields returns NoUpdateFields violation`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001"
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertViolationFields(
            result,
            FieldError.NoUpdateFields
        )
    }

    @Test
    fun `update with valid name returns success`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            name = "Updated warehouse"
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
    fun `update with only regional zone returns success`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            regionalZone = RegionalZone.SOUTH
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
    fun `update with only valid latitude returns success`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            latitude = 31.5
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
    fun `update with only valid longitude returns success`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            longitude = 34.4
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
    fun `update with blank name returns name violation`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            name = " "
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidWarehouseName
        )
    }

    @Test
    fun `update with invalid latitude returns latitude violation`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            latitude = 91.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidLatitude
        )
    }

    @Test
    fun `update with invalid longitude returns longitude violation`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            longitude = -181.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidLongitude
        )
    }

    @Test
    fun `update with multiple invalid fields returns all violations`() {
        // Given
        val input = UpdateWarehouseInput(
            id = "WH-001",
            name = " ",
            latitude = 91.0,
            longitude = -181.0
        )

        // When
        val result = validator.validateUpdate(input)

        // Then
        assertViolationFields(
            result,
            FieldError.InvalidWarehouseName,
            FieldError.InvalidLatitude,
            FieldError.InvalidLongitude
        )
    }

    private fun assertViolationFields(
        result: ValidationResult,
        vararg expectedFields: FieldError
    ) {
        val failure = assertIs<ValidationResult.Failure>(result)

        val actualFields = failure.violations.map {
            it.field
        }

        assertEquals(
            expectedFields.size,
            actualFields.size
        )

        assertTrue(
            actualFields.containsAll(
                expectedFields.toList()
            )
        )
    }

    private fun warehouse(
        name: String = "Main warehouse",
        latitude: Double = 31.5,
        longitude: Double = 34.4
    ) = Warehouse(
        id = "WH-001",
        name = name,
        regionalZone = RegionalZone.NORTH,
        latitude = latitude,
        longitude = longitude
    )
}

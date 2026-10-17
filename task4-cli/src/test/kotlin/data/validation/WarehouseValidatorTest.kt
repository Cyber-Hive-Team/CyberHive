package org.example.data.validation

import org.example.data.dataholder.WarehouseRaw
import org.example.domain.model.RegionalZone
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WarehouseValidatorTest {

    private val validator = WarehouseValidator()

    @Test
    fun `valid raw warehouse returns no warnings`() {
        // Given
        val raw = rawWarehouse()

        // When
        val warnings = validator.validate(raw)

        // Then
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `blank id returns warning`() {
        // Given
        val raw = rawWarehouse(id = " ")

        // When
        val warnings = validator.validate(raw)

        // Then
        assertEquals(
            listOf("Warning: Warehouse skipped - ID is missing"),
            warnings
        )
    }


    @Test
    fun `latitude below minimum returns warning`() {
        // Given
        val raw = rawWarehouse(latitude = -90.1)

        // When
        val warnings = validator.validate(raw)

        // Then
        assertEquals(
            listOf(
                "Warning: Warehouse WH-001 skipped - invalid latitude"
            ),
            warnings
        )
    }

    @Test
    fun `latitude above maximum returns warning`() {
        // Given
        val raw = rawWarehouse(latitude = 90.1)

        // When
        val warnings = validator.validate(raw)

        // Then
        assertEquals(
            listOf(
                "Warning: Warehouse WH-001 skipped - invalid latitude"
            ),
            warnings
        )
    }

    @Test
    fun `longitude below minimum returns warning`() {
        // Given
        val raw = rawWarehouse(longitude = -180.1)

        // When
        val warnings = validator.validate(raw)

        // Then
        assertEquals(
            listOf(
                "Warning: Warehouse WH-001 skipped - invalid longitude"
            ),
            warnings
        )
    }

    @Test
    fun `longitude above maximum returns warning`() {
        // Given
        val raw = rawWarehouse(longitude = 180.1)

        // When
        val warnings = validator.validate(raw)

        // Then
        assertEquals(
            listOf(
                "Warning: Warehouse WH-001 skipped - invalid longitude"
            ),
            warnings
        )
    }

    @Test
    fun `latitude boundaries are accepted`() {
        // Given
        val lower = rawWarehouse(latitude = -90.0)
        val upper = rawWarehouse(latitude = 90.0)

        // When
        val lowerWarnings = validator.validate(lower)
        val upperWarnings = validator.validate(upper)

        // Then
        assertTrue(lowerWarnings.isEmpty())
        assertTrue(upperWarnings.isEmpty())
    }

    @Test
    fun `longitude boundaries are accepted`() {
        // Given
        val lower = rawWarehouse(longitude = -180.0)
        val upper = rawWarehouse(longitude = 180.0)

        // When
        val lowerWarnings = validator.validate(lower)
        val upperWarnings = validator.validate(upper)

        // Then
        assertTrue(lowerWarnings.isEmpty())
        assertTrue(upperWarnings.isEmpty())
    }

    private fun rawWarehouse(
        id: String = "WH-001",
        latitude: Double = 31.5,
        longitude: Double = 34.4
    ) = WarehouseRaw(
        id = id,
        name = "Main warehouse",
        regionalZone = RegionalZone.NORTH,
        latitude = latitude,
        longitude = longitude
    )
}

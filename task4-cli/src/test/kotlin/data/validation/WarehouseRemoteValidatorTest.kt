package org.example.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WarehouseRemoteValidatorTest {

    private val validator = WarehouseRemoteValidator()

    @Test
    fun `valid dto passes validation`() {
        // Given
        val dto = dto()

        // When
        validator.validate(dto)

        // Then
        // No exception means validation passed successfully.
    }

    @Test
    fun `null regional zone throws exception`() {
        // Given
        val dto = dto(regionalZone = null)

        // When
        val error = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        // Then
        assertTrue(
            error.message.orEmpty().contains("regionalZone")
        )
    }

    @Test
    fun `blank regional zone throws exception`() {
        // Given
        val dto = dto(regionalZone = "   ")

        // When
        val error = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        // Then
        assertTrue(
            error.message.orEmpty().contains("regionalZone")
        )
    }

    @Test
    fun `null latitude throws exception`() {
        // Given
        val dto = dto(latitude = null)

        // When
        val error = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        // Then
        assertTrue(
            error.message.orEmpty().contains("latitude")
        )
    }

    @Test
    fun `null longitude throws exception`() {
        // Given
        val dto = dto(longitude = null)

        // When
        val error = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        // Then
        assertTrue(
            error.message.orEmpty().contains("longitude")
        )
    }

    @Test
    fun `missing required fields are reported together`() {
        // Given
        val dto = dto(
            regionalZone = null,
            latitude = null,
            longitude = null
        )

        // When
        val error = assertFailsWith<NullRequiredFieldException> {
            validator.validate(dto)
        }

        // Then
        val message = error.message.orEmpty()

        assertTrue(message.contains("regionalZone"))
        assertTrue(message.contains("latitude"))
        assertTrue(message.contains("longitude"))
    }

    private fun dto(
        regionalZone: String? = "NORTH",
        latitude: Double? = 31.5,
        longitude: Double? = 34.4
    ) = WarehouseResponseDto(
        id = "WH-001",
        name = "Main warehouse",
        regionalZone = regionalZone,
        latitude = latitude,
        longitude = longitude
    )
}

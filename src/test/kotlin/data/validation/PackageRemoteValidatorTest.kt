package org.example.test.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.data.validation.PackageRemoteValidator
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PackageRemoteValidatorTest {

    private val validator = PackageRemoteValidator()

    @Test
    fun `accepts dto with non-null weight`() {
        // Given
        val dto = createDto(weight = 5.0)

        // When / Then
        assertDoesNotThrow {
            validator.validate(dto)
        }
    }

    @Test
    fun `throws when weight is null`() {
        // Given
        val dto = createDto(weight = null)

        // When
        val error = assertThrows(
            NullRequiredFieldException::class.java
        ) {
            validator.validate(dto)
        }

        // Then
        assertEquals(
            "Package '${dto.id}' has null weight.",
            error.message
        )
    }

    private fun createDto(
        weight: Double?
    ): PackageResponseDto = PackageResponseDto(
        id = "PKG-000001",
        weight = weight,
        originHubId = "WH-001",
        destinationHubId = "WH-002",
        priority = "STANDARD"
    )
}

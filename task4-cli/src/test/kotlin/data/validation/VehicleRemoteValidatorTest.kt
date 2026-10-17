package org.example.test.data.validation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.data.validation.VehicleRemoteValidator
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
class VehicleRemoteValidatorTest {


        private val validator = VehicleRemoteValidator()

        @Test
        fun `accepts dto with required fields`() {
            // Given
            val dto = createDto()

            // When / Then
            assertDoesNotThrow {
                validator.validate(dto)
            }
        }

        @Test
        fun `throws when max capacity is null`() {
            // Given
            val dto = createDto(maxCapacityKg = null)

            // When
            val error = assertThrows(
                NullRequiredFieldException::class.java
            ) {
                validator.validate(dto)
            }

            // Then
            assertEquals(
                "Vehicle '${dto.vehicleId}' has null maxCapacityKg.",
                error.message
            )
        }

        @Test
        fun `throws when cost per km is null`() {
            // Given
            val dto = createDto(costPerKm = null)

            // When
            val error = assertThrows(
                NullRequiredFieldException::class.java
            ) {
                validator.validate(dto)
            }

            // Then
            assertEquals(
                "Vehicle '${dto.vehicleId}' has null costPerKm.",
                error.message
            )
        }

    private fun createDto(
        maxCapacityKg: Double? = 100.0,
        costPerKm: Double? = 2.5
    ): VehicleResponseDto = VehicleResponseDto(
        vehicleId = "TRK-0001",
        currentHubId = "WH-001",
        maxCapacityKg = maxCapacityKg,
        costPerKm = costPerKm,
        currentHub = WarehouseResponseDto(
            id = "WH-001",
            name = "Current Warehouse",
            regionalZone = null,
            latitude = null,
            longitude = null
        )
    )
    }


package org.example.test.data.validation


import org.example.data.dataholder.VehicleRaw
import org.example.data.validation.VehicleValidator
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class VehicleValidatorTest {

        private val factory = TestDataFactory()
        private val validator = VehicleValidator()
        private val hub = factory.createWarehouse("WH-001")

        @Test
        fun `returns no warnings for valid data`() {
            // Given
            val raw = createRaw()

            // When
            val result = validator.validate(raw, hub)

            // Then
            assertEquals(emptyList<String>(), result)
        }

        @Test
        fun `warns when vehicle id is blank`() {
            // Given
            val raw = createRaw(id = "")

            // When
            val result = validator.validate(raw, hub)

            // Then
            assertEquals(
                listOf("Warning: Vehicle skipped - missing id"),
                result
            )
        }

        @Test
        fun `warns when hub id is blank`() {
            // Given
            val raw = createRaw(currentHubId = "")

            // When
            val result = validator.validate(raw, hub)

            // Then
            assertEquals(
                listOf("Warning: Vehicle ${raw.id} skipped - missing hub id"),
                result
            )
        }

        @Test
        fun `warns when warehouse is missing`() {
            // Given
            val raw = createRaw()

            // When
            val result = validator.validate(raw, null)

            // Then
            assertEquals(
                listOf(
                    "Warning: Vehicle ${raw.id} skipped - " +
                            "warehouse not found: ${raw.currentHubId}"
                ),
                result
            )
        }

        @Test
        fun `warns when capacity is invalid`() {
            // Given
            val raw = createRaw(maxCapacityKg = 0.0)

            // When
            val result = validator.validate(raw, hub)

            // Then
            assertEquals(
                listOf("Warning: Vehicle ${raw.id} skipped - invalid capacity"),
                result
            )
        }

        @Test
        fun `warns when cost per km is invalid`() {
            // Given
            val raw = createRaw(costPerKm = -1.0)

            // When
            val result = validator.validate(raw, hub)

            // Then
            assertEquals(
                listOf("Warning: Vehicle ${raw.id} skipped - invalid cost per km"),
                result
            )
        }

        @Test
        fun `collects all applicable warnings`() {
            // Given
            val raw = createRaw(
                id = "",
                currentHubId = "",
                maxCapacityKg = 0.0,
                costPerKm = -1.0
            )

            // When
            val result = validator.validate(raw, null)

            // Then
            assertEquals(
                listOf(
                    "Warning: Vehicle skipped - missing id",
                    "Warning: Vehicle ${raw.id} skipped - missing hub id",
                    "Warning: Vehicle ${raw.id} skipped - " +
                            "warehouse not found: ${raw.currentHubId}",
                    "Warning: Vehicle ${raw.id} skipped - invalid capacity",
                    "Warning: Vehicle ${raw.id} skipped - invalid cost per km"
                ),
                result
            )
        }

        private fun createRaw(
            id: String = "TRK-0001",
            currentHubId: String = "WH-001",
            maxCapacityKg: Double = 100.0,
            costPerKm: Double = 2.5
        ): VehicleRaw = VehicleRaw(
            id = id,
            currentHubId = currentHubId,
            maxCapacityKg = maxCapacityKg,
            costPerKm = costPerKm
        )
    }


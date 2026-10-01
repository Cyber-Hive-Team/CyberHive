package org.example.test.data.validation

import org.example.data.dataholder.PackageRaw
import org.example.data.validation.PackageValidator
import org.example.domain.model.Priority
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PackageValidatorTest {
    private val factory = TestDataFactory()
    private val validator = PackageValidator()
    private val origin = factory.createWarehouse("WH-001")
    private val destination = factory.createWarehouse("WH-002")

    @Test
    fun `returns no warnings for valid data`() {
        // Given
        val raw = createRaw()

        // When
        val result = validator.validate(raw, origin, destination)

        // Then
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `warns when package id is blank`() {
        // Given
        val raw = createRaw(id = "")

        // When
        val result = validator.validate(raw, origin, destination)

        // Then
        assertEquals(
            listOf("Warning: Package skipped - missing id"),
            result
        )
    }

    @Test
    fun `warns when origin warehouse is missing`() {
        // Given
        val raw = createRaw()

        // When
        val result = validator.validate(raw, null, destination)

        // Then
        assertEquals(
            listOf(
                "Warning: Package ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}"
            ),
            result
        )
    }

    @Test
    fun `warns when destination warehouse is missing`() {
        // Given
        val raw = createRaw()

        // When
        val result = validator.validate(raw, origin, null)

        // Then
        assertEquals(
            listOf(
                "Warning: Package ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            ),
            result
        )
    }

    @Test
    fun `collects all applicable warnings`() {
        // Given
        val raw = createRaw(id = "")

        // When
        val result = validator.validate(raw, null, null)

        // Then
        assertEquals(
            listOf(
                "Warning: Package skipped - missing id",
                "Warning: Package ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}",
                "Warning: Package ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            ),
            result
        )
    }

    private fun createRaw(
        id: String = "PKG-000001"
    ): PackageRaw = PackageRaw(
        id = id,
        weight = 5.0,
        originHubId = origin.id,
        destinationHubId = destination.id,
        priority = Priority.STANDARD
    )

}

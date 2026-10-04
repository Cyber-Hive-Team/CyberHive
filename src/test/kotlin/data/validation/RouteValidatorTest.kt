package org.example.test.data.validation

import org.example.data.dataholder.RouteRaw
import org.example.data.validation.RouteValidator
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class RouteValidatorTest {

    private val validator = RouteValidator()

    private val originWarehouse = Warehouse(
        id = "WH-001",
        name = "Origin Warehouse",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.5
    )

    private val destinationWarehouse = Warehouse(
        id = "WH-002",
        name = "Destination Warehouse",
        regionalZone = RegionalZone.SOUTH,
        latitude = 31.4,
        longitude = 34.6
    )

    private fun createValidRaw(
        id: String = "RT-00001",
        originHubId: String = "WH-001",
        destinationHubId: String = "WH-002",
        distanceKm: Double = 100.0,
        typicalDelayMin: Int = 10
    ): RouteRaw {
        return RouteRaw(
            id = id,
            originHubId = originHubId,
            destinationHubId = destinationHubId,
            distanceKm = distanceKm,
            typicalDelayMin = typicalDelayMin
        )
    }

    @Test
    fun `valid route should return no warnings`() {
        // Given
        val raw = createValidRaw()

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // Then
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `route with blank id should return missing id warning`() {
        // Given
        val raw = createValidRaw(id = "")

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // Then
        assertEquals(
            listOf("Warning: Route skipped - missing id"),
            warnings
        )
    }

    @Test
    fun `route with missing origin warehouse should return warning`() {
        // Given
        val raw = createValidRaw(
            originHubId = "WH-999"
        )

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = null,
            destination = destinationWarehouse
        )

        // Then
        assertEquals(
            listOf(
                "Warning: Route RT-00001 skipped - " +
                        "origin warehouse not found: WH-999"
            ),
            warnings
        )
    }

    @Test
    fun `route with missing destination warehouse should return warning`() {
        // Given
        val raw = createValidRaw(
            destinationHubId = "WH-999"
        )

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = null
        )

        // Then
        assertEquals(
            listOf(
                "Warning: Route RT-00001 skipped - " +
                        "destination warehouse not found: WH-999"
            ),
            warnings
        )
    }

    @Test
    fun `route with zero distance should return invalid distance warning`() {
        // Given
        val raw = createValidRaw(distanceKm = 0.0)

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // Then
        assertEquals(
            listOf(
                "Warning: Route RT-00001 skipped - invalid distance"
            ),
            warnings
        )
    }

    @Test
    fun `route with negative distance should return invalid distance warning`() {
        // Given
        val raw = createValidRaw(distanceKm = -10.0)

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // Then
        assertEquals(
            listOf(
                "Warning: Route RT-00001 skipped - invalid distance"
            ),
            warnings
        )
    }

    @Test
    fun `route with negative delay should return invalid delay warning`() {
        // Given
        val raw = createValidRaw(typicalDelayMin = -5)

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // Then
        assertEquals(
            listOf(
                "Warning: Route RT-00001 skipped - invalid delay"
            ),
            warnings
        )
    }

    @Test
    fun `route with zero delay should return no warnings`() {
        // Given
        val raw = createValidRaw(typicalDelayMin = 0)

        // When
        val warnings = validator.validate(
            raw = raw,
            origin = originWarehouse,
            destination = destinationWarehouse
        )

        // Then
        assertTrue(warnings.isEmpty())
    }

    @Test
    fun `route with multiple invalid fields should return multiple warnings`() {
        // Given
        val raw = createValidRaw(
            id = "",
            originHubId = "WH-999",
            destinationHubId = "WH-888",
            distanceKm = -10.0,
            typicalDelayMin = -5
        )

        // When
        val warnings = validator.validate(
            raw,
            null,
            null
        )

        // Then
        assertEquals(
            listOf(
                "Warning: Route skipped - missing id",
                "Warning: Route  skipped - origin warehouse not found: WH-999",
                "Warning: Route  skipped - destination warehouse not found: WH-888",
                "Warning: Route  skipped - invalid distance",
                "Warning: Route  skipped - invalid delay"
            ),
            warnings
        )
    }
}

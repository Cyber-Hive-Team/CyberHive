package org.example.test.domain.algorithm.dynamicprogramming

import org.example.domain.algorithm.dynamicprogramming.TwoDimensionalKnapsackCargoOptimizer
import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.domain.model.Priority
import org.example.domain.model.Vehicle
import org.example.test.TestDataFactory

class TwoDimensionalKnapsackCargoOptimizerTest {

    private val factory = TestDataFactory()
    private val optimizer = TwoDimensionalKnapsackCargoOptimizer()

    @Test
    fun `selects package when both weight and volume fit within vehicle limits`() {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        val vehicle = Vehicle(
            id = "TRK-0001",
            maxCapacityKg = 15.0,
            maxVolumeM3 = 10.0,
            costPerKm = 1.0,
            currentHub = warehouse
        )
        val cargoPackage = factory.createPackage(
            id = "PKG-000001",
            weight = 8.0,
            priority = Priority.URGENT
        ).copy(volumeM3 = 5.0)

        // When
        val result = optimizer.selectOptimalPackages(
            vehicle = vehicle,
            packages = listOf(cargoPackage)
        )

        // Then
        assertEquals(listOf(cargoPackage), result)
    }

    @Test
    fun `does not select package when its volume exceeds vehicle volume capacity`() {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        val vehicle = Vehicle(
            id = "TRK-0001",
            maxCapacityKg = 15.0,
            maxVolumeM3 = 5.0,
            costPerKm = 1.0,
            currentHub = warehouse
        )
        val bulkyPackage = factory.createPackage(
            id = "PKG-000001",
            weight = 8.0,
            priority = Priority.URGENT
        ).copy(volumeM3 = 8.0)

        // When
        val result = optimizer.selectOptimalPackages(
            vehicle = vehicle,
            packages = listOf(bulkyPackage)
        )

        // Then
        assertEquals(emptyList(), result)
    }

    @Test
    fun `does not select package when its weight exceeds vehicle weight capacity`() {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        val vehicle = Vehicle(
            id = "TRK-0001",
            maxCapacityKg = 10.0,
            maxVolumeM3 = 10.0,
            costPerKm = 1.0,
            currentHub = warehouse
        )
        val heavyPackage = factory.createPackage(
            id = "PKG-000001",
            weight = 15.0,
            priority = Priority.URGENT
        ).copy(volumeM3 = 5.0)

        // When
        val result = optimizer.selectOptimalPackages(
            vehicle = vehicle,
            packages = listOf(heavyPackage)
        )

        // Then
        assertEquals(emptyList(), result)
    }

    @Test
    fun `prefers smaller volume package when bulky low weight package competes for capacity`() {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        val vehicle = Vehicle(
            id = "TRK-0001",
            maxCapacityKg = 20.0,
            maxVolumeM3 = 10.0,
            costPerKm = 1.0,
            currentHub = warehouse
        )
        val bulkyPackage = factory.createPackage(
            id = "PKG-000001",
            weight = 5.0,
            priority = Priority.STANDARD
        ).copy(volumeM3 = 9.0)
        val compactPackage = factory.createPackage(
            id = "PKG-000002",
            weight = 5.0,
            priority = Priority.STANDARD
        ).copy(volumeM3 = 3.0)

        // When
        val result = optimizer.selectOptimalPackages(
            vehicle = vehicle,
            packages = listOf(bulkyPackage, compactPackage)
        )

        // Then
        assertEquals(listOf(compactPackage), result)
    }

    @Test
    fun `prefers lighter package when dense high weight package competes for weight capacity`() {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        val vehicle = Vehicle(
            id = "TRK-0001",
            maxCapacityKg = 10.0,
            maxVolumeM3 = 20.0,
            costPerKm = 1.0,
            currentHub = warehouse
        )
        val densePackage = factory.createPackage(
            id = "PKG-000001",
            weight = 9.0,
            priority = Priority.STANDARD
        ).copy(volumeM3 = 2.0)
        val lighterPackage = factory.createPackage(
            id = "PKG-000002",
            weight = 5.0,
            priority = Priority.STANDARD
        ).copy(volumeM3 = 3.0)

        // When
        val result = optimizer.selectOptimalPackages(
            vehicle = vehicle,
            packages = listOf(densePackage, lighterPackage)
        )

        // Then
        assertEquals(listOf(lighterPackage), result)
    }

    @Test
    fun `2d optimizer respects volume constraint when choosing optimal cargo`() {
        // Given
        val vehicle = createComparisonVehicle()
        val packages = createComparisonPackages()

        // When
        val result = optimizer.selectOptimalPackages(vehicle, packages)

        // Then
        assertEquals(listOf(packages[1], packages[2]), result)
    }

    private fun createComparisonVehicle(): Vehicle {
        val warehouse = factory.createWarehouse("WH-001")

        return Vehicle(
            id = "TRK-0001",
            maxCapacityKg = 15.0,
            maxVolumeM3 = 10.0,
            costPerKm = 1.0,
            currentHub = warehouse
        )
    }

    private fun createComparisonPackages() = listOf(
        factory.createPackage(
            id = "PKG-000001",
            weight = 8.0,
            priority = Priority.URGENT
        ).copy(volumeM3 = 9.0),

        factory.createPackage(
            id = "PKG-000002",
            weight = 7.0,
            priority = Priority.STANDARD
        ).copy(volumeM3 = 4.0),

        factory.createPackage(
            id = "PKG-000003",
            weight = 7.0,
            priority = Priority.STANDARD
        ).copy(volumeM3 = 5.0)
    )
}

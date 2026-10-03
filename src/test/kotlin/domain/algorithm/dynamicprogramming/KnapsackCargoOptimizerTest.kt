package org.example.domain.algorithm.dynamicprogramming

import kotlin.test.Test
import kotlin.test.assertEquals
import org.example.domain.model.Priority
import org.example.domain.model.Vehicle
import org.example.test.TestDataFactory

class KnapsackCargoOptimizerTest {

    private val factory = TestDataFactory()
    private val optimizer = KnapsackCargoOptimizer()

    @Test
    fun `selects package when it fits within vehicle capacity`() {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        val vehicle = Vehicle(id = "TRK-0001", maxCapacityKg = 15.0, costPerKm = 1.0, currentHub = warehouse)
        val cargoPackage = factory.createPackage(id = "PKG-000001", weight = 8.5, priority = Priority.URGENT)
        // When
        val result = optimizer.selectOptimalPackages(vehicle = vehicle, packages = listOf(cargoPackage))
        // Then
        assertEquals(listOf(cargoPackage), result)
    }
}

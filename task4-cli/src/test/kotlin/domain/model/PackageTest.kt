package org.example.test.domain.model

import io.mockk.mockk
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Warehouse
import org.example.domain.state.AssignedToVehicleState
import org.example.domain.state.CreatedState
import org.example.domain.state.DeliveredState
import org.example.domain.state.DeliveryFailedState
import org.example.domain.state.InTransitState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PackageTest {

    private val originWarehouse = mockk<Warehouse>()
    private val destinationWarehouse = mockk<Warehouse>()

    private fun createPackage(): Package {
        return Package(
            id = "PKG-123456",
            weight = 10.0,
            priority = Priority.LOW,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    @Test
    fun `new package should start in CreatedState`() {
        // Given
        val packageItem = createPackage()

        // When
        val currentState = packageItem.state

        // Then
        assertEquals(CreatedState::class, currentState::class)
    }

    @Test
    fun `assignToVehicle should change state to AssignedToVehicleState`() {
        // Given
        val packageItem = createPackage()

        // When
        packageItem.assignToVehicle()

        // Then
        assertEquals(AssignedToVehicleState::class, packageItem.state::class)
    }

    @Test
    fun `startTransit should change state to InTransitState`() {
        // Given
        val packageItem = createPackage()
        packageItem.assignToVehicle()

        // When
        packageItem.startTransit()

        // Then
        assertEquals(InTransitState::class, packageItem.state::class)
    }

    @Test
    fun `markDelivered should change state to DeliveredState`() {
        // Given
        val packageItem = createPackage()
        packageItem.assignToVehicle()
        packageItem.startTransit()

        // When
        packageItem.markDelivered()

        // Then
        assertEquals(DeliveredState::class, packageItem.state::class)
    }

    @Test
    fun `markDeliveryFailed should change state to DeliveryFailedState`() {
        // Given
        val packageItem = createPackage()
        packageItem.assignToVehicle()
        packageItem.startTransit()

        // When
        packageItem.markDeliveryFailed()

        // Then
        assertEquals(DeliveryFailedState::class, packageItem.state::class)
    }
}

package org.example.test.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException
import org.example.domain.state.DeliveredState
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DeliveredStateTest {

    private val state = DeliveredState()

    @Test
    fun `assignToVehicle should throw exception`() {
        // Given
        val currentState = state

        // When
        val action = {
            currentState.assignToVehicle()
        }

        // Then
        assertThrows(InvalidPackageStateTransitionException::class.java) {
            action()
        }
    }

    @Test
    fun `startTransit should throw exception`() {
        // Given
        val currentState = state

        // When
        val action = {
            currentState.startTransit()
        }

        // Then
        assertThrows(InvalidPackageStateTransitionException::class.java) {
            action()
        }
    }

    @Test
    fun `markDelivered should throw exception`() {
        // Given
        val currentState = state

        // When
        val action = {
            currentState.markDelivered()
        }

        // Then
        assertThrows(InvalidPackageStateTransitionException::class.java) {
            action()
        }
    }

    @Test
    fun `markDeliveryFailed should throw exception`() {
        // Given
        val currentState = state

        // When
        val action = {
            currentState.markDeliveryFailed()
        }

        // Then
        assertThrows(InvalidPackageStateTransitionException::class.java) {
            action()
        }
    }
}

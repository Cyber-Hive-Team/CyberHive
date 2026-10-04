package org.example.test.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException
import org.example.domain.state.DeliveredState
import org.example.domain.state.DeliveryFailedState
import org.example.domain.state.InTransitState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class InTransitStateTest {

    private val state = InTransitState()

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
    fun `markDelivered should transition to DeliveredState`() {
        // Given
        val currentState = state

        // When
        val nextState = currentState.markDelivered()

        // Then
        assertEquals(DeliveredState::class, nextState::class)
    }

    @Test
    fun `markDeliveryFailed should transition to DeliveryFailedState`() {
        // Given
        val currentState = state

        // When
        val nextState = currentState.markDeliveryFailed()

        // Then
        assertEquals(DeliveryFailedState::class, nextState::class)
    }
}

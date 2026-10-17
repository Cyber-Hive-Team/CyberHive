package org.example.test.domain.state

import org.example.domain.model.exception.InvalidPackageStateTransitionException
import org.example.domain.state.AssignedToVehicleState
import org.example.domain.state.CreatedState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CreatedStateTest {

    private val state = CreatedState()

    @Test
    fun `assignToVehicle should transition to AssignedToVehicleState`() {
        // Given
        val currentState = state

        // When
        val nextState = currentState.assignToVehicle()

        // Then
        assertEquals(AssignedToVehicleState::class, nextState::class)
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

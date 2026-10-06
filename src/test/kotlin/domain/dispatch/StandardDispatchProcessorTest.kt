package org.example.test.domain.dispatch

import io.mockk.mockk
import io.mockk.verify
import org.example.domain.dispatch.DispatchNotifier
import org.example.domain.dispatch.StandardDispatchProcessor
import org.example.domain.dispatch.VehicleCapacityReservations
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Test

class StandardDispatchProcessorTest {

    private val factory = TestDataFactory()
    private val reservations = VehicleCapacityReservations()
    private val notifier = mockk<DispatchNotifier>(relaxed = true)
    private val processor = StandardDispatchProcessor(capacityReservations = reservations, notifier = notifier)
    private val vehicle = factory.createVehicle()
    private val cargoPackage = factory.createPackage(weight = CARGO_WEIGHT_KG)

    @Test
    fun `should send standard notification when standard dispatch succeeds`() {
        // Given
        val expectedMessage = "STANDARD: $PACKAGE_COUNT packages dispatched " + "on vehicle ${vehicle.id}"

        // When
        processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))

        // Then
        verify(exactly = EXPECTED_NOTIFICATION_CALLS) {
            notifier.notify(expectedMessage)
        }
    }

    companion object {
        private const val CARGO_WEIGHT_KG = 30.0
        private const val PACKAGE_COUNT = 1
        private const val EXPECTED_NOTIFICATION_CALLS = 1
    }
}

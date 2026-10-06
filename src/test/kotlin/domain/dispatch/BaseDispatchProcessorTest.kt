package org.example.test.domain.dispatch

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.domain.dispatch.DispatchNotifier
import org.example.domain.dispatch.StandardDispatchProcessor
import org.example.domain.dispatch.VehicleCapacityReservations
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.state.CreatedState
import org.example.domain.state.InTransitState
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseDispatchProcessorTest {

    private val factory = TestDataFactory()
    private val reservations = VehicleCapacityReservations()
    private val notifier = mockk<DispatchNotifier>(relaxed = true)

    private val processor = StandardDispatchProcessor(capacityReservations = reservations, notifier = notifier)
    private val vehicle = factory.createVehicle()

    @Test
    fun `should move package to InTransitState when valid cargo is dispatched`() {
        // Given
        val cargoPackage = factory.createPackage(weight = VALID_CARGO_WEIGHT_KG)
        // When
        processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        // Then
        assertTrue(cargoPackage.state is InTransitState)
    }

    @Test
    fun `should reject dispatch when cargo is empty`() {
        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = emptyList())
        }
    }

    @Test
    fun `should reject dispatch when cargo contains duplicate packages`() {
        // Given
        val firstPackage = factory.createPackage(id = DUPLICATE_PACKAGE_ID)
        val secondPackage = factory.createPackage(id = DUPLICATE_PACKAGE_ID)
        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(firstPackage, secondPackage))
        }
    }

    @Test
    fun `should reject package when weight is zero`() {
        // Given
        val cargoPackage = factory.createPackage(weight = ZERO_WEIGHT_KG)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should reject package when weight is negative`() {
        // Given
        val cargoPackage = factory.createPackage(weight = NEGATIVE_WEIGHT_KG)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should reject package when weight is NaN`() {
        // Given
        val cargoPackage = factory.createPackage(weight = Double.NaN)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should reject package when weight is infinite`() {
        // Given
        val cargoPackage = factory.createPackage(weight = Double.POSITIVE_INFINITY)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should reject package when origin warehouse differs from vehicle hub`() {
        // Given
        val differentWarehouse = factory.createWarehouse(DIFFERENT_WAREHOUSE_ID)

        val cargoPackage = factory.createPackage(origin = differentWarehouse)
        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should reject package when package is already assigned`() {
        // Given
        val cargoPackage = factory.createPackage()
        cargoPackage.assignToVehicle()

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            processor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should not reserve capacity when cargo validation fails`() {
        // Given
        val cargoPackage = factory.createPackage(weight = NEGATIVE_WEIGHT_KG)
        val mockReservations = mockk<VehicleCapacityReservations>(relaxed = true)
        val testProcessor = StandardDispatchProcessor(capacityReservations = mockReservations, notifier = notifier)

        // When
        runCatching {
            testProcessor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }

        // Then
        verify(exactly = NO_CALLS) {
            mockReservations.reserve(any(), any())
        }
    }

    @Test
    fun `should reserve capacity before changing package state when dispatch succeeds`() {
        // Given
        val cargoPackage = factory.createPackage(weight = VALID_CARGO_WEIGHT_KG)
        val mockReservations = mockk<VehicleCapacityReservations>()
        var wasCreatedDuringReservation = false

        every {
            mockReservations.reserve(vehicle = vehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        } answers {
            wasCreatedDuringReservation = cargoPackage.state is CreatedState
        }

        val testProcessor = StandardDispatchProcessor(capacityReservations = mockReservations, notifier = notifier)

        // When
        testProcessor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        // Then
        assertTrue(wasCreatedDuringReservation)
    }

    @Test
    fun `should keep package in CreatedState when capacity reservation fails`() {
        // Given
        val cargoPackage = factory.createPackage(weight = VALID_CARGO_WEIGHT_KG)
        val mockReservations = mockk<VehicleCapacityReservations>()

        every {
            mockReservations.reserve(vehicle = vehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        } throws EntityValidationException(RESERVATION_FAILURE_MESSAGE)

        val testProcessor = StandardDispatchProcessor(capacityReservations = mockReservations, notifier = notifier)

        // When
        runCatching {
            testProcessor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }

        // Then
        assertTrue(cargoPackage.state is CreatedState)
    }

    @Test
    fun `should propagate exception when capacity reservation fails`() {
        // Given
        val cargoPackage = factory.createPackage(weight = VALID_CARGO_WEIGHT_KG)
        val mockReservations = mockk<VehicleCapacityReservations>()

        every {
            mockReservations.reserve(vehicle = vehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        } throws EntityValidationException(RESERVATION_FAILURE_MESSAGE)
        val testProcessor = StandardDispatchProcessor(capacityReservations = mockReservations, notifier = notifier)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            testProcessor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        }
    }

    @Test
    fun `should notify after package state is updated when dispatch succeeds`() {
        // Given
        val cargoPackage = factory.createPackage(weight = VALID_CARGO_WEIGHT_KG)
        val testNotifier = mockk<DispatchNotifier>()
        var wasInTransitDuringNotification = false

        every {
            testNotifier.notify(any())
        } answers {
            wasInTransitDuringNotification = cargoPackage.state is InTransitState
        }
        val testProcessor = StandardDispatchProcessor(capacityReservations = reservations, notifier = testNotifier)
        // When
        testProcessor.dispatch(vehicle = vehicle, cargo = listOf(cargoPackage))
        // Then
        assertTrue(wasInTransitDuringNotification)
    }

    companion object {
        private const val VALID_CARGO_WEIGHT_KG = 40.0
        private const val ZERO_WEIGHT_KG = 0.0
        private const val NEGATIVE_WEIGHT_KG = -1.0
        private const val NO_CALLS = 0
        private const val DUPLICATE_PACKAGE_ID = "PKG-000001"
        private const val DIFFERENT_WAREHOUSE_ID = "WH-003"
        private const val RESERVATION_FAILURE_MESSAGE = "Capacity reservation failed"
    }
}

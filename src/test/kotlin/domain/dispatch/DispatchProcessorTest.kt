package org.example.test.domain.dispatch

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.domain.dispatch.ExpressDispatchProcessor
import org.example.domain.dispatch.StandardDispatchProcessor
import org.example.domain.dispatch.VehicleCapacityReservations
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.state.AssignedToVehicleState
import org.example.domain.state.CreatedState
import org.example.domain.state.InTransitState
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DispatchProcessorTest {

    private val factory = TestDataFactory()
    private val reservations = VehicleCapacityReservations()

    private val standard = StandardDispatchProcessor(reservations)
    private val express = ExpressDispatchProcessor(reservations)

    @Test
    fun standardDispatchSucceedsAtExactCapacity() {
        // Given
        val vehicle = factory.createVehicle(maxCapacityKg = 50.0)
        val cargoPackage = factory.createPackage(weight = 50.0)

        // When
        standard.dispatch(vehicle, listOf(cargoPackage))

        // Then
        assertTrue(cargoPackage.state is InTransitState)

        assertEquals(
            50.0,
            reservations.reservedWeightFor(vehicle.id),
            0.001
        )
    }

    @Test
    fun expressDispatchSucceeds() {
        // Given
        val vehicle = factory.createVehicle()
        val cargoPackage = factory.createPackage(weight = 30.0)

        // When
        express.dispatch(vehicle, listOf(cargoPackage))

        // Then
        assertTrue(cargoPackage.state is InTransitState)

        assertEquals(
            30.0,
            reservations.reservedWeightFor(vehicle.id),
            0.001
        )
    }

    @Test
    fun cargoExceedingCapacityIsRejected() {
        // Given
        val vehicle = factory.createVehicle(maxCapacityKg = 50.0)
        val cargoPackage = factory.createPackage(weight = 51.0)

        // When
        assertThrows(EntityValidationException::class.java) {
            standard.dispatch(vehicle, listOf(cargoPackage))
        }

        // Then
        assertTrue(cargoPackage.state is CreatedState)

        assertEquals(
            0.0,
            reservations.reservedWeightFor(vehicle.id),
            0.001
        )
    }

    @Test
    fun emptyCargoIsRejected() {
        // Given
        val vehicle = factory.createVehicle()

        // When
        assertThrows(EntityValidationException::class.java) {
            standard.dispatch(vehicle, emptyList())
        }

        // Then
        assertEquals(
            0.0,
            reservations.reservedWeightFor(vehicle.id),
            0.001
        )
    }

    @Test
    fun alreadyAssignedPackageIsRejected() {
        // Given
        val vehicle = factory.createVehicle()
        val cargoPackage = factory.createPackage()

        cargoPackage.assignToVehicle()

        // When
        assertThrows(EntityValidationException::class.java) {
            standard.dispatch(vehicle, listOf(cargoPackage))
        }

        // Then
        assertTrue(cargoPackage.state is AssignedToVehicleState)

        assertEquals(
            0.0,
            reservations.reservedWeightFor(vehicle.id),
            0.001
        )
    }

    @Test
    fun invalidCargoIsRejectedBeforeAnyPackageChanges() {
        // Given
        val vehicle = factory.createVehicle()
        val duplicate = factory.createPackage()

        val invalidCargoLists = listOf(
            listOf(factory.createPackage(weight = -1.0)),
            listOf(factory.createPackage(weight = 0.0)),
            listOf(factory.createPackage(weight = Double.NaN)),
            listOf(
                factory.createPackage(
                    weight = Double.POSITIVE_INFINITY
                )
            ),
            listOf(
                factory.createPackage(
                    origin = factory.createWarehouse("WH-003")
                )
            ),
            listOf(duplicate, duplicate),
            listOf(
                factory.createPackage(
                    id = "PKG-000001",
                    weight = 10.0
                ),
                factory.createPackage(
                    id = "PKG-000002",
                    weight = -5.0
                )
            )
        )

        invalidCargoLists.forEach { cargo ->
            // When
            assertThrows(EntityValidationException::class.java) {
                standard.dispatch(vehicle, cargo)
            }

            // Then
            assertTrue(cargo.all { it.state is CreatedState })

            assertEquals(
                0.0,
                reservations.reservedWeightFor(vehicle.id),
                0.001
            )
        }
    }

    @Test
    fun expressRespectsCapacityReservedByStandard() {
        // Given
        val vehicle = factory.createVehicle(maxCapacityKg = 100.0)

        val first = factory.createPackage(
            id = "PKG-000001",
            weight = 60.0
        )

        val second = factory.createPackage(
            id = "PKG-000002",
            weight = 50.0
        )

        standard.dispatch(vehicle, listOf(first))

        // When
        assertThrows(EntityValidationException::class.java) {
            express.dispatch(vehicle, listOf(second))
        }

        // Then
        assertTrue(first.state is InTransitState)
        assertTrue(second.state is CreatedState)

        assertEquals(
            60.0,
            reservations.reservedWeightFor(vehicle.id),
            0.001
        )
    }

    @Test
    fun capacityIsReservedBeforeStateChanges() {
        // Given
        val vehicle = factory.createVehicle()
        val cargoPackage = factory.createPackage(weight = 40.0)

        val mockReservations = mockk<VehicleCapacityReservations>()
        val processor = StandardDispatchProcessor(mockReservations)

        every {
            mockReservations.reserve(vehicle, 40.0)
        } answers {
            assertTrue(cargoPackage.state is CreatedState)
        }

        // When
        processor.dispatch(vehicle, listOf(cargoPackage))

        // Then
        verify(exactly = 1) {
            mockReservations.reserve(vehicle, 40.0)
        }

        assertTrue(cargoPackage.state is InTransitState)
    }

    @Test
    fun reservationFailureStopsShipmentStateChanges() {
        // Given
        val vehicle = factory.createVehicle()
        val cargoPackage = factory.createPackage(weight = 40.0)

        val mockReservations = mockk<VehicleCapacityReservations>()
        val processor = StandardDispatchProcessor(mockReservations)

        val error = EntityValidationException(
            "Capacity reservation failed"
        )

        every {
            mockReservations.reserve(vehicle, 40.0)
        } throws error

        // When
        val thrown = assertThrows(
            EntityValidationException::class.java
        ) {
            processor.dispatch(vehicle, listOf(cargoPackage))
        }

        // Then
        assertSame(error, thrown)
        assertTrue(cargoPackage.state is CreatedState)

        verify(exactly = 1) {
            mockReservations.reserve(vehicle, 40.0)
        }
    }
}

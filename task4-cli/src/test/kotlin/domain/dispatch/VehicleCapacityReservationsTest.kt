package org.example.test.domain.dispatch

import org.example.domain.dispatch.VehicleCapacityReservations
import org.example.domain.model.exception.EntityValidationException
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class VehicleCapacityReservationsTest {

    private val factory = TestDataFactory()
    private val reservations = VehicleCapacityReservations()
    private val vehicle = factory.createVehicle(maxCapacityKg = VEHICLE_CAPACITY_KG)

    @Test
    fun `should reserve cargo weight when cargo fits vehicle capacity`() {
        // When
        reservations.reserve(vehicle = vehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)

        // Then
        assertEquals(
            VALID_CARGO_WEIGHT_KG,
            reservations.reservedWeightFor(vehicle.id),
            DOUBLE_TOLERANCE
        )
    }

    @Test
    fun `should reserve full capacity when cargo weight equals vehicle capacity`() {
        // When
        reservations.reserve(vehicle = vehicle, cargoWeight = VEHICLE_CAPACITY_KG)

        // Then
        assertEquals(
            VEHICLE_CAPACITY_KG,
            reservations.reservedWeightFor(vehicle.id),
            DOUBLE_TOLERANCE
        )
    }

    @Test
    fun `should accumulate reserved weight when vehicle receives multiple reservations`() {
        // Given
        reservations.reserve(vehicle = vehicle, cargoWeight = FIRST_RESERVATION_WEIGHT_KG)

        // When
        reservations.reserve(vehicle = vehicle, cargoWeight = SECOND_RESERVATION_WEIGHT_KG)

        // Then
        assertEquals(
            TOTAL_RESERVED_WEIGHT_KG,
            reservations.reservedWeightFor(vehicle.id),
            DOUBLE_TOLERANCE
        )
    }

    @Test
    fun `should reject reservation when remaining capacity is exceeded`() {
        // Given
        reservations.reserve(vehicle = vehicle, cargoWeight = FIRST_RESERVATION_WEIGHT_KG)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            reservations.reserve(vehicle = vehicle, cargoWeight = OVERFLOW_WEIGHT_KG)
        }
    }

    @Test
    fun `should keep previous reserved weight when additional reservation fails`() {
        // Given
        reservations.reserve(vehicle = vehicle, cargoWeight = FIRST_RESERVATION_WEIGHT_KG)

        // When
        runCatching {
            reservations.reserve(vehicle = vehicle, cargoWeight = OVERFLOW_WEIGHT_KG)
        }

        // Then
        assertEquals(
            FIRST_RESERVATION_WEIGHT_KG,
            reservations.reservedWeightFor(vehicle.id),
            DOUBLE_TOLERANCE
        )
    }

    @Test
    fun `should reject reservation when cargo weight exceeds vehicle capacity`() {
        // When & Then
        assertThrows(
            EntityValidationException::class.java
        ) {
            reservations.reserve(vehicle = vehicle, cargoWeight = OVER_CAPACITY_WEIGHT_KG)
        }
    }

    @Test
    fun `should reject reservation when cargo weight is zero`() {
        // When & Then
        assertThrows(EntityValidationException::class.java) {
            reservations.reserve(vehicle = vehicle, cargoWeight = ZERO_WEIGHT_KG)
        }
    }

    @Test
    fun `should reject reservation when cargo weight is negative`() {
        // When & Then
        assertThrows(
            EntityValidationException::class.java
        ) {
            reservations.reserve(vehicle = vehicle, cargoWeight = NEGATIVE_WEIGHT_KG)
        }
    }

    @Test
    fun `should reject reservation when cargo weight is NaN`() {
        // When & Then
        assertThrows(EntityValidationException::class.java) {
            reservations.reserve(vehicle = vehicle, cargoWeight = Double.NaN)
        }
    }

    @Test
    fun `should reject reservation when cargo weight is infinite`() {
        // When & Then
        assertThrows(
            EntityValidationException::class.java
        ) {
            reservations.reserve(vehicle = vehicle, cargoWeight = Double.POSITIVE_INFINITY)
        }
    }

    @Test
    fun `should reject reservation when vehicle capacity is zero`() {
        // Given
        val invalidVehicle = factory.createVehicle(maxCapacityKg = ZERO_WEIGHT_KG)

        // When & Then
        assertThrows(
            EntityValidationException::class.java
        ) {
            reservations.reserve(vehicle = invalidVehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        }
    }

    @Test
    fun `should reject reservation when vehicle capacity is NaN`() {
        // Given
        val invalidVehicle = factory.createVehicle(maxCapacityKg = Double.NaN)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            reservations.reserve(vehicle = invalidVehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        }
    }

    @Test
    fun `should reject reservation when vehicle capacity is negative`() {
        // Given
        val invalidVehicle = factory.createVehicle(maxCapacityKg = NEGATIVE_WEIGHT_KG)

        // When & Then
        assertThrows(
            EntityValidationException::class.java
        ) {
            reservations.reserve(vehicle = invalidVehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        }
    }

    @Test
    fun `should reject reservation when vehicle capacity is infinite`() {
        // Given
        val invalidVehicle = factory.createVehicle(maxCapacityKg = Double.POSITIVE_INFINITY)

        // When & Then
        assertThrows(EntityValidationException::class.java) {
            reservations.reserve(vehicle = invalidVehicle, cargoWeight = VALID_CARGO_WEIGHT_KG)
        }
    }

    @Test
    fun `should track first vehicle reservation independently when vehicles differ`() {
        // Given
        val firstVehicle = factory.createVehicle(id = FIRST_VEHICLE_ID, maxCapacityKg = VEHICLE_CAPACITY_KG)
        val secondVehicle = factory.createVehicle(id = SECOND_VEHICLE_ID, maxCapacityKg = VEHICLE_CAPACITY_KG)
        reservations.reserve(vehicle = firstVehicle, cargoWeight = FIRST_VEHICLE_WEIGHT_KG)
        // When
        reservations.reserve(vehicle = secondVehicle, cargoWeight = SECOND_VEHICLE_WEIGHT_KG)

        // Then
        assertEquals(
            FIRST_VEHICLE_WEIGHT_KG,
            reservations.reservedWeightFor(firstVehicle.id),
            DOUBLE_TOLERANCE
        )
    }

    @Test
    fun `should track second vehicle reservation independently when vehicles differ`() {
        // Given
        val firstVehicle = factory.createVehicle(id = FIRST_VEHICLE_ID, maxCapacityKg = VEHICLE_CAPACITY_KG)
        val secondVehicle = factory.createVehicle(id = SECOND_VEHICLE_ID, maxCapacityKg = VEHICLE_CAPACITY_KG)
        reservations.reserve(vehicle = firstVehicle, cargoWeight = FIRST_VEHICLE_WEIGHT_KG)

        // When
        reservations.reserve(vehicle = secondVehicle, cargoWeight = SECOND_VEHICLE_WEIGHT_KG)
        // Then
        assertEquals(
            SECOND_VEHICLE_WEIGHT_KG,
            reservations.reservedWeightFor(secondVehicle.id),
            DOUBLE_TOLERANCE
        )
    }

    companion object {
        private const val VEHICLE_CAPACITY_KG = 100.0
        private const val VALID_CARGO_WEIGHT_KG = 40.0
        private const val FIRST_RESERVATION_WEIGHT_KG = 60.0
        private const val SECOND_RESERVATION_WEIGHT_KG = 30.0
        private const val TOTAL_RESERVED_WEIGHT_KG = 90.0
        private const val OVERFLOW_WEIGHT_KG = 50.0
        private const val OVER_CAPACITY_WEIGHT_KG = 101.0
        private const val ZERO_WEIGHT_KG = 0.0
        private const val NEGATIVE_WEIGHT_KG = -1.0
        private const val FIRST_VEHICLE_WEIGHT_KG = 30.0
        private const val SECOND_VEHICLE_WEIGHT_KG = 50.0
        private const val DOUBLE_TOLERANCE = 0.001
        private const val FIRST_VEHICLE_ID = "TRK-0001"
        private const val SECOND_VEHICLE_ID = "TRK-0002"
    }
}

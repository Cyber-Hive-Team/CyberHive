package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.result.VehicleTrackingResult
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.TrackVehicleCurrentLocationUseCase
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrackVehicleCurrentLocationUseCaseTest {

    private val vehicleRepository = mockk<VehicleRepository>()

    private val useCase = TrackVehicleCurrentLocationUseCase(
        vehicleRepository
    )

    private val warehouse = Warehouse(
        id = "WH-001",
        name = "Main Warehouse",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.4
    )

    private val vehicle = Vehicle(
        id = "TRK-0001",
        maxCapacityKg = 100.0,
        costPerKm = 2.0,
        currentHub = warehouse
    )

    @Test
    fun `when vehicle exists should return success`() = runBlocking {
        // Given
        coEvery {
            vehicleRepository.getById("TRK-0001")
        } returns Result.success(vehicle)

        // When
        val result = useCase("TRK-0001")

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `when vehicle exists should return current location`() = runBlocking {
        // Given
        coEvery {
            vehicleRepository.getById("TRK-0001")
        } returns Result.success(vehicle)

        // When
        val result = useCase("TRK-0001")

        // Then
        assertEquals(
            VehicleTrackingResult(
                vehicleId = "TRK-0001",
                currentWarehouseId = "WH-001",
                currentWarehouseName = "Main Warehouse"
            ),
            result.getOrThrow()
        )

        coVerify(exactly = 1) {
            vehicleRepository.getById("TRK-0001")
        }
    }


    @Test
    fun `returns failure when vehicle is not found`() =
        runBlocking {
            // Given
            val error = IllegalStateException(
                "Vehicle not found"
            )

            coEvery {
                vehicleRepository.getById("TRK-9999")
            } returns Result.failure(error)

            // When
            val result = useCase("TRK-9999")

            // Then
            assertAll(
                { assertTrue(result.isFailure) },
                { assertEquals(error, result.exceptionOrNull()) }
            )

            coVerify(exactly = 1) {
                vehicleRepository.getById("TRK-9999")
            }
        }

    @Test
    fun `returns correct vehicle id`() = runBlocking {
        // Given
        coEvery {
            vehicleRepository.getById("TRK-0001")
        } returns Result.success(vehicle)

        // When
        val result = useCase("TRK-0001")

        // Then
        assertEquals(
            "TRK-0001",
            result.getOrThrow().vehicleId
        )

        coVerify(exactly = 1) {
            vehicleRepository.getById("TRK-0001")
        }
    }

    @Test
    fun `returns correct warehouse information`() = runBlocking {
        // Given
        coEvery {
            vehicleRepository.getById("TRK-0001")
        } returns Result.success(vehicle)

        // When
        val result = useCase("TRK-0001")
        val trackingResult = result.getOrThrow()

        // Then
        assertAll(
            {
                assertEquals(
                    "WH-001",
                    trackingResult.currentWarehouseId
                )
            },
            {
                assertEquals(
                    "Main Warehouse",
                    trackingResult.currentWarehouseName
                )
            }
        )

        coVerify(exactly = 1) {
            vehicleRepository.getById("TRK-0001")
        }
    }
}

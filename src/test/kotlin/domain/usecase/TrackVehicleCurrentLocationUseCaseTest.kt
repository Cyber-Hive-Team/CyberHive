package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.result.VehicleTrackingResult
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.TrackVehicleCurrentLocationUseCase
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun `returns vehicle current location successfully`() {
        runBlocking {

            coEvery {
                vehicleRepository.getById("TRK-0001")
            } returns Result.success(vehicle)

            val result = useCase("TRK-0001")

            assertTrue(result.isSuccess)

            assertEquals(
                VehicleTrackingResult(
                    vehicleId = "TRK-0001",
                    currentWarehouseId = "WH-001",
                    currentWarehouseName = "Main Warehouse"
                ),
                result.getOrThrow()
            )
        }
    }

    @Test
    fun `returns failure when vehicle is not found`() {
        runBlocking {

            val error = IllegalStateException(
                "Vehicle not found"
            )

            coEvery {
                vehicleRepository.getById("TRK-9999")
            } returns Result.failure(error)

            val result = useCase("TRK-9999")

            assertTrue(result.isFailure)

            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `returns correct vehicle id`() {
        runBlocking {

            coEvery {
                vehicleRepository.getById("TRK-0001")
            } returns Result.success(vehicle)

            val result = useCase("TRK-0001")

            assertEquals(
                "TRK-0001",
                result.getOrThrow().vehicleId
            )
        }
    }

    @Test
    fun `returns correct warehouse information`() {
        runBlocking {

            coEvery {
                vehicleRepository.getById("TRK-0001")
            } returns Result.success(vehicle)

            val result = useCase("TRK-0001")

            val trackingResult = result.getOrThrow()

            assertEquals(
                "WH-001",
                trackingResult.currentWarehouseId
            )

            assertEquals(
                "Main Warehouse",
                trackingResult.currentWarehouseName
            )
        }
    }
}

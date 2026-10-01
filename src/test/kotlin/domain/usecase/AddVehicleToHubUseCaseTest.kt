package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.input.AddVehicleToHubInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.AddVehicleToHubUseCase
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AddVehicleToHubUseCaseTest {

    private val vehicleRepository = mockk<VehicleRepository>()
    private val warehouseRepository = mockk<WarehouseRepository>()

    private val useCase = AddVehicleToHubUseCase(
        vehicleRepository,
        warehouseRepository
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

    private val input = AddVehicleToHubInput(
        vehicleId = "TRK-0001",
        warehouseId = "WH-001"
    )

    @Test
    fun `adds vehicle to warehouse successfully`() {
        runBlocking {

            coEvery {
                vehicleRepository.getById("TRK-0001")
            } returns Result.success(vehicle)

            coEvery {
                warehouseRepository.getById("WH-001")
            } returns Result.success(warehouse)

            val result = useCase(input)

            assertTrue(result.isSuccess)

            assertEquals(
                vehicle,
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
                vehicleRepository.getById("TRK-0001")
            } returns Result.failure(error)

            val result = useCase(input)

            assertTrue(result.isFailure)

            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `returns failure when warehouse is not found`() {
        runBlocking {

            val error = IllegalStateException(
                "Warehouse not found"
            )

            coEvery {
                vehicleRepository.getById("TRK-0001")
            } returns Result.success(vehicle)

            coEvery {
                warehouseRepository.getById("WH-001")
            } returns Result.failure(error)

            val result = useCase(input)

            assertTrue(result.isFailure)

            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }
}

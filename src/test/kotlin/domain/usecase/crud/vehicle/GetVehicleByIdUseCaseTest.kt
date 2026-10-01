package org.example.test.domain.usecase.crud.vehicle

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.crud.vehicle.GetVehicleByIdUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetVehicleByIdUseCaseTest {

        private val factory = TestDataFactory()
        private val repository = mockk<VehicleRepository>()
        private val useCase = GetVehicleByIdUseCase(repository)

        @Test
        fun `returns requested vehicle`() = runBlocking {
            val vehicle = factory.createVehicle()

            coEvery { repository.getById(vehicle.id) } returns Result.success(vehicle)

            val result = useCase(vehicle.id)

            assertEquals(vehicle, result.getOrThrow())
            coVerify(exactly = 1) { repository.getById(vehicle.id) }
        }

        @Test
        fun `preserves vehicle not found failure`() = runBlocking {
            val vehicleId = "TRK-9999"
            val error = VehicleNotFoundException("Vehicle not found")

            coEvery { repository.getById(vehicleId) } returns Result.failure(error)

            val result = useCase(vehicleId)

            assertSame(error, result.exceptionOrNull())
        }

        @Test
        fun `preserves repository failure`() = runBlocking {
            val vehicleId = "TRK-0001"
            val error = IllegalStateException("Read failed")

            coEvery { repository.getById(vehicleId) } returns Result.failure(error)

            val result = useCase(vehicleId)

            assertSame(error, result.exceptionOrNull())
        }
    }


package org.example.test.domain.usecase.crud.vehicle
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.crud.vehicle.DeleteVehicleUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DeleteVehicleUseCaseTest {

        private val factory = TestDataFactory()
        private val repository = mockk<VehicleRepository>()
        private val useCase = DeleteVehicleUseCase(repository)

        @Test
        fun `returns deletion response when vehicle exists`() = runBlocking {
            val vehicle = factory.createVehicle()
            val response = "Vehicle deleted"

            coEvery { repository.getById(vehicle.id) } returns Result.success(vehicle)
            coEvery { repository.delete(vehicle.id) } returns Result.success(response)

            val result = useCase(vehicle.id)

            assertEquals(response, result.getOrThrow())
            coVerify(exactly = 1) { repository.delete(vehicle.id) }
        }

        @Test
        fun `does not delete when vehicle is missing`() = runBlocking {
            val vehicleId = "TRK-9999"
            val error = VehicleNotFoundException("Vehicle not found")

            coEvery { repository.getById(vehicleId) } returns Result.failure(error)

            val result = useCase(vehicleId)

            assertSame(error, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.delete(any()) }
        }

        @Test
        fun `preserves deletion failure`() = runBlocking {
            val vehicle = factory.createVehicle()
            val error = IllegalStateException("Delete failed")

            coEvery { repository.getById(vehicle.id) } returns Result.success(vehicle)
            coEvery { repository.delete(vehicle.id) } returns Result.failure(error)

            val result = useCase(vehicle.id)

            assertSame(error, result.exceptionOrNull())
        }
    }


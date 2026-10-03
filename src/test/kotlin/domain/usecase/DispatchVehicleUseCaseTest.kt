package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.DispatchVehicleUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

    class DispatchVehicleUseCaseTest {
        private val factory = TestDataFactory()
        private val vehicleRepository = mockk<VehicleRepository>()
        private val packageRepository = mockk<PackageRepository>()
        private val useCase = DispatchVehicleUseCase(vehicleRepository, packageRepository)

        @Test
        fun `loads packages from vehicle current hub without exceeding capacity`() = runBlocking {
            val hub = factory.createWarehouse("WH-001")
            val otherHub = factory.createWarehouse("WH-002")
            val vehicle = factory.createVehicle(maxCapacityKg = 100.0, currentHub = hub)
            val first = factory.createPackage(id = "PKG-000001", weight = 40.0, origin = hub)
            val second = factory.createPackage(id = "PKG-000002", weight = 50.0, origin = hub)
            val tooHeavy = factory.createPackage(id = "PKG-000003", weight = 30.0, origin = hub)
            val wrongHub = factory.createPackage(id = "PKG-000004", weight = 10.0, origin = otherHub)

            coEvery { vehicleRepository.getVehicles() } returns Result.success(listOf(vehicle))
            coEvery { packageRepository.getAllPackages() } returns Result.success(
                listOf(first, second, tooHeavy, wrongHub)
            )

            val result = useCase(vehicle.id)

            assertEquals(listOf(first, second), result.getOrThrow())
        }

        @Test
        fun `fails when vehicle is not found`() = runBlocking {
            coEvery { vehicleRepository.getVehicles() } returns Result.success(emptyList())

            val result = useCase("TRK-9999")

            assertInstanceOf(VehicleNotFoundException::class.java, result.exceptionOrNull())
        }

        @Test
        fun `preserves vehicle repository failure`() = runBlocking {
            val error = IllegalStateException("Vehicles failed")

            coEvery { vehicleRepository.getVehicles() } returns Result.failure(error)

            val result = useCase("TRK-0001")

            assertSame(error, result.exceptionOrNull())
        }

        @Test
        fun `preserves package repository failure`() = runBlocking {
            val vehicle = factory.createVehicle()
            val error = IllegalStateException("Packages failed")

            coEvery { vehicleRepository.getVehicles() } returns Result.success(listOf(vehicle))
            coEvery { packageRepository.getAllPackages() } returns Result.failure(error)

            val result = useCase(vehicle.id)

            assertSame(error, result.exceptionOrNull())
        }
    }


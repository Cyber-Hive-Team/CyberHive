package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.InvalidRequiredWeightException
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.FindStationedVehiclesByCapacityUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FindStationedVehiclesByCapacityUseCaseTest {

        private val factory = TestDataFactory()
        private val repository = mockk<VehicleRepository>()
        private val useCase = FindStationedVehiclesByCapacityUseCase(repository)

        @Test
        fun `returns vehicles at warehouse with enough capacity`() = runBlocking {
            val warehouse = factory.createWarehouse("WH-001")
            val otherWarehouse = factory.createWarehouse("WH-002")
            val matching = factory.createVehicle(id = "TRK-0001", maxCapacityKg = 100.0, currentHub = warehouse)
            val tooSmall = factory.createVehicle(id = "TRK-0002", maxCapacityKg = 40.0, currentHub = warehouse)
            val elsewhere = factory.createVehicle(id = "TRK-0003", maxCapacityKg = 150.0, currentHub = otherWarehouse)

            coEvery { repository.getVehicles() } returns Result.success(listOf(matching, tooSmall, elsewhere))

            val result = useCase(warehouse, 80.0)

            assertEquals(listOf(matching), result.getOrThrow())
        }

        @Test
        fun `fails when required weight is not positive`() = runBlocking {
            val warehouse = factory.createWarehouse("WH-001")

            val result = useCase(warehouse, 0.0)

            assertInstanceOf(InvalidRequiredWeightException::class.java, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.getVehicles() }
        }

        @Test
        fun `preserves repository failure`() = runBlocking {
            val warehouse = factory.createWarehouse("WH-001")
            val error = IllegalStateException("Read failed")

            coEvery { repository.getVehicles() } returns Result.failure(error)

            val result = useCase(warehouse, 50.0)

            assertSame(error, result.exceptionOrNull())
        }
    }

package org.example.test.domain.usecase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.VehicleReassignmentFailedException
import org.example.domain.model.result.FleetShortageResult
import org.example.domain.model.result.FleetSurplusResult
import org.example.domain.model.result.VehicleTransferResult
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.FindFleetShortageUseCase
import org.example.domain.usecase.FindFleetSurplusUseCase
import org.example.domain.usecase.RedistributeFleetUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

    class RedistributeFleetUseCaseTest {
        private val factory = TestDataFactory()
        private val shortageUseCase = mockk<FindFleetShortageUseCase>()
        private val surplusUseCase = mockk<FindFleetSurplusUseCase>()
        private val vehicleRepository = mockk<VehicleRepository>()
        private val useCase = RedistributeFleetUseCase(shortageUseCase, surplusUseCase, vehicleRepository)

        @Test
        fun `transfers vehicles from surplus warehouse to shortage warehouse`() = runBlocking {
            val surplusHub = factory.createWarehouse("WH-001")
            val vehicle = factory.createVehicle(id = "TRK-0001", maxCapacityKg = 80.0, currentHub = surplusHub)

            coEvery { shortageUseCase() } returns listOf(FleetShortageResult("WH-002", 80.0))
            coEvery { surplusUseCase() } returns listOf(FleetSurplusResult("WH-001", 100.0))
            coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-001") } returns Result.success(listOf(vehicle))
            coEvery { vehicleRepository.reassignVehicle("TRK-0001", "WH-002") } returns Result.success(true)

            val result = useCase()

            assertEquals(
                listOf(VehicleTransferResult("TRK-0001", "WH-001", "WH-002", 80.0)),
                result.getOrThrow()
            )
            coVerify(exactly = 1) { vehicleRepository.reassignVehicle("TRK-0001", "WH-002") }
        }

        @Test
        fun `returns empty transfers when there are no shortages`() = runBlocking {
            coEvery { shortageUseCase() } returns emptyList()
            coEvery { surplusUseCase() } returns listOf(FleetSurplusResult("WH-001", 100.0))

            val result = useCase()

            assertEquals(emptyList<VehicleTransferResult>(), result.getOrThrow())
            coVerify(exactly = 0) { vehicleRepository.getVehiclesByWarehouseId(any()) }
        }

        @Test
        fun `fails when vehicle reassignment returns false`(): Unit = runBlocking {
            val surplusHub = factory.createWarehouse("WH-001")
            val vehicle = factory.createVehicle(id = "TRK-0001", maxCapacityKg = 80.0, currentHub = surplusHub)

            coEvery { shortageUseCase() } returns listOf(FleetShortageResult("WH-002", 80.0))
            coEvery { surplusUseCase() } returns listOf(FleetSurplusResult("WH-001", 100.0))
            coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-001") } returns Result.success(listOf(vehicle))
            coEvery { vehicleRepository.reassignVehicle("TRK-0001", "WH-002") } returns Result.success(false)

            val result = useCase()

            assertInstanceOf(VehicleReassignmentFailedException::class.java, result.exceptionOrNull())
        }

        @Test
        fun `preserves repository failure`() = runBlocking {
            val error = IllegalStateException("Vehicles failed")

            coEvery { shortageUseCase() } returns listOf(FleetShortageResult("WH-002", 80.0))
            coEvery { surplusUseCase() } returns listOf(FleetSurplusResult("WH-001", 100.0))
            coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-001") } returns Result.failure(error)

            val result = useCase()

            assertSame(error, result.exceptionOrNull())
        }
    }


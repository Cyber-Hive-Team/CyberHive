package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.InvalidPackageWeightException
import org.example.domain.model.result.FleetShortageResult
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.FindFleetShortageUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class FindFleetShortageUseCaseTest {
    private val factory = TestDataFactory()
    private val warehouseRepository = mockk<WarehouseRepository>()
    private val packageRepository = mockk<PackageRepository>()
    private val vehicleRepository = mockk<VehicleRepository>()
    private val useCase = FindFleetShortageUseCase(warehouseRepository, packageRepository, vehicleRepository)

    @Test
    fun `returns shortages sorted by shortage amount`() = runBlocking {
        val firstWarehouse = factory.createWarehouse("WH-001")
        val secondWarehouse = factory.createWarehouse("WH-002")

        coEvery { warehouseRepository.getAllWarehouses() } returns Result.success(
            listOf(firstWarehouse, secondWarehouse)
        )
        coEvery { packageRepository.getPackagesByWarehouseId("WH-001") } returns Result.success(
            listOf(factory.createPackage(id = "PKG-000001", weight = 120.0, origin = firstWarehouse))
        )
        coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-001") } returns Result.success(
            listOf(factory.createVehicle(maxCapacityKg = 50.0, currentHub = firstWarehouse))
        )
        coEvery { packageRepository.getPackagesByWarehouseId("WH-002") } returns Result.success(
            listOf(factory.createPackage(id = "PKG-000002", weight = 90.0, origin = secondWarehouse))
        )
        coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-002") } returns Result.success(
            listOf(factory.createVehicle(id = "TRK-0002", maxCapacityKg = 20.0, currentHub = secondWarehouse))
        )

        val result = useCase()

        Assertions.assertEquals(
            listOf(
                FleetShortageResult("WH-001", 70.0),
                FleetShortageResult("WH-002", 70.0)
            ), result)
    }

    @Test
    fun `returns empty list when there is no shortage`() = runBlocking {
        val warehouse = factory.createWarehouse("WH-001")

        coEvery { warehouseRepository.getAllWarehouses() } returns Result.success(listOf(warehouse))
        coEvery { packageRepository.getPackagesByWarehouseId("WH-001") } returns Result.success(
            listOf(factory.createPackage(weight = 50.0, origin = warehouse))
        )
        coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-001") } returns Result.success(
            listOf(factory.createVehicle(maxCapacityKg = 100.0, currentHub = warehouse))
        )

        Assertions.assertEquals(emptyList<FleetShortageResult>(), useCase())
    }

    @Test
    fun `throws when package weight is invalid`() {
        val warehouse = factory.createWarehouse("WH-001")

        coEvery { warehouseRepository.getAllWarehouses() } returns Result.success(listOf(warehouse))
        coEvery { packageRepository.getPackagesByWarehouseId("WH-001") } returns Result.success(
            listOf(factory.createPackage(weight = -1.0, origin = warehouse))
        )
        coEvery { vehicleRepository.getVehiclesByWarehouseId("WH-001") } returns Result.success(emptyList())

        Assertions.assertThrows(InvalidPackageWeightException::class.java) {
            runBlocking { useCase() }
        }
    }
}

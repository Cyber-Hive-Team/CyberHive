package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.InvalidPackageWeightException
import org.example.domain.model.exception.InvalidVehicleCapacityException
import org.example.domain.model.result.FleetSurplusResult
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.FindFleetSurplusUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class FindFleetSurplusUseCaseTest {

    private val factory = TestDataFactory()
    private val warehouseRepository = mockk<WarehouseRepository>()
    private val packageRepository = mockk<PackageRepository>()
    private val vehicleRepository = mockk<VehicleRepository>()

    private val useCase = FindFleetSurplusUseCase(
        warehouseRepository,
        packageRepository,
        vehicleRepository
    )

    @Test
    fun `calculates available fleet capacity`() = runBlocking {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        stubWarehouses(warehouse)
        stubWarehouseData(
            warehouse = warehouse,
            packageWeight = 60.0,
            vehicleCapacity = 180.0
        )

        // When
        val result = useCase()

        // Then
        assertEquals(
            listOf(FleetSurplusResult("WH-001", 120.0)),
            result
        )
    }

    @Test
    fun `orders warehouses by highest surplus first`() = runBlocking {
        // Given
        val first = factory.createWarehouse("WH-001")
        val second = factory.createWarehouse("WH-002")

        stubWarehouses(first, second)
        stubWarehouseData(first, packageWeight = 50.0, vehicleCapacity = 130.0)
        stubWarehouseData(second, packageWeight = 50.0, vehicleCapacity = 200.0)

        // When
        val result = useCase()

        // Then
        assertEquals(
            listOf(
                FleetSurplusResult("WH-002", 150.0),
                FleetSurplusResult("WH-001", 80.0)
            ),
            result
        )
    }

    @Test
    fun `excludes warehouse with no surplus`() = runBlocking {
        // Given
        val warehouse = factory.createWarehouse("WH-001")
        stubWarehouses(warehouse)
        stubWarehouseData(
            warehouse = warehouse,
            packageWeight = 100.0,
            vehicleCapacity = 100.0
        )

        // When
        val result = useCase()

        // Then
        assertEquals(emptyList<FleetSurplusResult>(), result)
    }

    @Test
    fun `rejects package with negative weight`() {
        runBlocking {
            // Given
            val warehouse = factory.createWarehouse("WH-001")
            stubWarehouses(warehouse)
            stubWarehouseData(
                warehouse = warehouse,
                packageWeight = -1.0,
                vehicleCapacity = 100.0
            )

            // When
            val result = runCatching { useCase() }

            // Then
            assertInstanceOf(
                InvalidPackageWeightException::class.java,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `rejects vehicle with negative capacity`() {
        runBlocking {
            // Given
            val warehouse = factory.createWarehouse("WH-001")
            stubWarehouses(warehouse)
            stubWarehouseData(
                warehouse = warehouse,
                packageWeight = 50.0,
                vehicleCapacity = -1.0
            )

            // When
            val result = runCatching { useCase() }

            // Then
            assertInstanceOf(
                InvalidVehicleCapacityException::class.java,
                result.exceptionOrNull()
            )
        }
    }

    private fun stubWarehouses(vararg warehouses: Warehouse) {
        coEvery {
            warehouseRepository.getAllWarehouses()
        } returns Result.success(warehouses.toList())
    }

    private fun stubWarehouseData(
        warehouse: Warehouse,
        packageWeight: Double,
        vehicleCapacity: Double
    ) {
        val packages = listOf(
            factory.createPackage(
                weight = packageWeight,
                origin = warehouse
            )
        )

        val vehicles = listOf(
            factory.createVehicle(
                maxCapacityKg = vehicleCapacity,
                currentHub = warehouse
            )
        )

        coEvery {
            packageRepository.getPackagesByWarehouseId(warehouse.id)
        } returns Result.success(packages)

        coEvery {
            vehicleRepository.getVehiclesByWarehouseId(warehouse.id)
        } returns Result.success(vehicles)
    }
}

package org.example.domain.usecase

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test
import kotlin.test.*

class GetWarehouseLoadFactorUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val useCase = GetWarehouseLoadFactorUseCase(repository)

    @Test
    fun `load factor is calculated from total cargo weight and fleet capacity`() = runBlocking {
        // Given
        val warehouse = warehouse()
        warehouse.addPackages(listOf(cargo(20.0), cargo(30.0)))
        warehouse.addVehicles(listOf(vehicle(40.0), vehicle(60.0)))
        coEvery { repository.getById(warehouse.id) } returns Result.success(warehouse)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertEquals(0.5, result.getOrThrow(), 0.000001)
    }

    @Test
    fun `empty cargo queue returns zero load factor`() = runBlocking {
        // Given
        val warehouse = warehouse()
        warehouse.addVehicles(listOf(vehicle(100.0)))
        coEvery { repository.getById(warehouse.id) } returns Result.success(warehouse)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertEquals(0.0, result.getOrThrow())
    }

    @Test
    fun `zero fleet capacity returns zero load factor`() = runBlocking {
        // Given
        val warehouse = warehouse()
        warehouse.addPackages(listOf(cargo(50.0)))
        coEvery {
            repository.getById(warehouse.id)
        } returns Result.success(warehouse)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertEquals(0.0, result.getOrThrow())
    }

    @Test
    fun `cargo weight above fleet capacity returns load factor greater than one`() = runBlocking {
        // Given
        val warehouse = warehouse()
        warehouse.addPackages(listOf(cargo(150.0)))
        warehouse.addVehicles(listOf(vehicle(100.0)))
        coEvery {
            repository.getById(warehouse.id)
        } returns Result.success(warehouse)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertEquals(1.5, result.getOrThrow(), 0.000001)
    }

    @Test
    fun `repository failure is returned`() = runBlocking {
        // Given
        val warehouse = warehouse()
        val error = WarehouseNotFoundException()
        coEvery { repository.getById(warehouse.id) } returns Result.failure(error)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertSame(error, result.exceptionOrNull())
    }

    private fun warehouse() =
        Warehouse(
            "WH-001",
            "Main",
            RegionalZone.NORTH,
            31.5,
            34.4
        )

    private fun cargo(weightKg: Double): Package = mockk {
        every { weight } returns weightKg
    }

    private fun vehicle(capacityKg: Double): Vehicle = mockk {
        every { maxCapacityKg } returns capacityKg
    }
}

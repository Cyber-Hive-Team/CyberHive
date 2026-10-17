package org.example.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AssignPackageToCargoQueueUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val useCase = AssignPackageToCargoQueueUseCase(repository)
    private val warehouseId = "WH-001"

    private val cargo = Package(
        id = "PKG-000001",
        weight = 5.0,
        priority = Priority.STANDARD,
        originWarehouse = warehouse("WH-001"),
        destinationWarehouse = warehouse("WH-002"),
        baseRate = 10.0
    )

    @Test
    fun `existing package is not added again`() = runBlocking {
        // Given
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.success(true)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertFalse(result.getOrThrow())
        coVerify(exactly = 0) { repository.addPackageToCargoQueue(any(), any()) }
        coVerify(exactly = 0) { repository.sortCargoQueue(any()) }
    }

    @Test
    fun `new package is added then queue is sorted`() = runBlocking {
        // Given
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.success(false)
        coEvery {
            repository.addPackageToCargoQueue(warehouseId, cargo)
        } returns Result.success(true)
        coEvery {
            repository.sortCargoQueue(warehouseId)
        } returns Result.success(true)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertTrue(result.getOrThrow())
        coVerifyOrder {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
            repository.addPackageToCargoQueue(warehouseId, cargo)
            repository.sortCargoQueue(warehouseId)
        }
    }

    @Test
    fun `unsuccessful addition prevents sorting`() = runBlocking {
        // Given
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.success(false)
        coEvery {
            repository.addPackageToCargoQueue(warehouseId, cargo)
        } returns Result.success(false)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertFalse(result.getOrThrow())
        coVerify(exactly = 0) { repository.sortCargoQueue(any()) }
    }

    @Test
    fun `existence check failure prevents addition and sorting`() = runBlocking {
        // Given
        val error = IllegalStateException("Check failed")
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.failure(error)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertSame(error, result.exceptionOrNull())
        coVerify(exactly = 0) { repository.addPackageToCargoQueue(any(), any()) }
        coVerify(exactly = 0) { repository.sortCargoQueue(any()) }
    }

    @Test
    fun `addition failure is returned without sorting`() = runBlocking {
        // Given
        val error = IllegalStateException("Add failed")
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.success(false)
        coEvery {
            repository.addPackageToCargoQueue(warehouseId, cargo)
        } returns Result.failure(error)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertSame(error, result.exceptionOrNull())
        coVerify(exactly = 0) { repository.sortCargoQueue(any()) }
    }

    @Test
    fun `unsuccessful sorting returns false`() = runBlocking {
        // Given
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.success(false)
        coEvery {
            repository.addPackageToCargoQueue(warehouseId, cargo)
        } returns Result.success(true)
        coEvery {
            repository.sortCargoQueue(warehouseId)
        } returns Result.success(false)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertFalse(result.getOrThrow())
    }

    @Test
    fun `sorting failure is returned`() = runBlocking {
        // Given
        val error = IllegalStateException("Sort failed")
        coEvery {
            repository.isPackageInCargoQueue(warehouseId, cargo.id)
        } returns Result.success(false)
        coEvery {
            repository.addPackageToCargoQueue(warehouseId, cargo)
        } returns Result.success(true)
        coEvery {
            repository.sortCargoQueue(warehouseId)
        } returns Result.failure(error)

        // When
        val result = useCase(warehouseId, cargo)

        // Then
        assertSame(error, result.exceptionOrNull())
    }

    private fun warehouse(id: String) =
        Warehouse(
            id,
            "Warehouse $id",
            RegionalZone.NORTH,
            31.5,
            34.4
        )
}

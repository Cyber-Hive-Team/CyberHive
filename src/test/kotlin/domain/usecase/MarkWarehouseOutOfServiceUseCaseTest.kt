package org.example.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseStatus
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MarkWarehouseOutOfServiceUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val useCase = MarkWarehouseOutOfServiceUseCase(repository)

    private val warehouse = Warehouse(
        "WH-001",
        "Main",
        RegionalZone.NORTH,
        31.5,
        34.4
    )

    @Test
    fun `successful status update returns out of service result`() = runBlocking {
        // Given
        coEvery {
            repository.getById(warehouse.id)
        } returns Result.success(warehouse)
        coEvery {
            repository.updateStatus(warehouse.id, WarehouseStatus.OUT_OF_SERVICE)
        } returns Result.success(true)

        // When
        val result = useCase(warehouse.id)

        // Then
        val status = result.getOrThrow()
        assertEquals(warehouse.id, status.warehouseId)
        assertEquals(warehouse.name, status.warehouseName)
        assertEquals(WarehouseStatus.OUT_OF_SERVICE, status.status)

        coVerify(exactly = 1) {
            repository.updateStatus(
                warehouse.id,
                WarehouseStatus.OUT_OF_SERVICE
            )
        }
    }

    @Test
    fun `missing warehouse prevents status update`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException()
        coEvery {
            repository.getById(warehouse.id)
        } returns Result.failure(error)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertSame(error, result.exceptionOrNull())
        coVerify(exactly = 0) { repository.updateStatus(any(), any()) }
    }

    @Test
    fun `status update failure is returned`() = runBlocking {
        // Given
        val error = IllegalStateException("Status update failed")
        coEvery {
            repository.getById(warehouse.id)
        } returns Result.success(warehouse)
        coEvery {
            repository.updateStatus(warehouse.id, WarehouseStatus.OUT_OF_SERVICE)
        } returns Result.failure(error)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }
}

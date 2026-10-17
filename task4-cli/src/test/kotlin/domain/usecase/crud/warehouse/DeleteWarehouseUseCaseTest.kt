package org.example.domain.usecase.crud.warehouse

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class DeleteWarehouseUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val useCase = DeleteWarehouseUseCase(repository)

    private val warehouse = Warehouse(
        "WH-001", "Main", RegionalZone.NORTH, 31.5, 34.4
    )

    @Test
    fun `existing warehouse is deleted`() = runBlocking {
        // Given
        coEvery { repository.getById(warehouse.id) } returns
                Result.success(warehouse)
        coEvery { repository.delete(warehouse.id) } returns
                Result.success("Deleted")

        // When
        val result = useCase(warehouse.id)

        // Then
        assertEquals("Deleted", result.getOrThrow())
        coVerifyOrder {
            repository.getById(warehouse.id)
            repository.delete(warehouse.id)
        }
    }

    @Test
    fun `missing warehouse prevents deletion`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException()
        coEvery { repository.getById(warehouse.id) } returns
                Result.failure(error)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertSame(error, result.exceptionOrNull())
        coVerify(exactly = 0) { repository.delete(any()) }
    }

    @Test
    fun `delete failure is returned`() = runBlocking {
        // Given
        val error = IllegalStateException("Delete failed")
        coEvery { repository.getById(warehouse.id) } returns
                Result.success(warehouse)
        coEvery { repository.delete(warehouse.id) } returns
                Result.failure(error)

        // When
        val result = useCase(warehouse.id)

        // Then
        assertSame(error, result.exceptionOrNull())
    }
}

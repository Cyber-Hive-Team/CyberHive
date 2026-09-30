package org.example.domain.usecase.crud.warehouse

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test
import kotlin.test.*

class GetWarehouseByIdUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val useCase = GetWarehouseByIdUseCase(repository)

    @Test
    fun `existing id returns warehouse`() = runBlocking {
        // Given
        val warehouse = Warehouse(
            "WH-001", "Main", RegionalZone.NORTH, 31.5, 34.4
        )
        coEvery { repository.getById("WH-001") } returns
                Result.success(warehouse)

        // When
        val result = useCase("WH-001")

        // Then
        assertSame(warehouse, result.getOrThrow())
        coVerify(exactly = 1) { repository.getById("WH-001") }
    }

    @Test
    fun `missing warehouse returns failure`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException()
        coEvery { repository.getById("WH-999") } returns
                Result.failure(error)

        // When
        val result = useCase("WH-999")

        // Then
        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `repository exception becomes failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Repository failed")
        coEvery { repository.getById("WH-001") } throws error

        // When
        val result = useCase("WH-001")

        // Then
        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }
}

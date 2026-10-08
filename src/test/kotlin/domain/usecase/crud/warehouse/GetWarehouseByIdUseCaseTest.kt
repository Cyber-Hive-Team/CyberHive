package org.example.domain.usecase.crud.warehouse

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test

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
    fun `when warehouse is missing should return failure`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException()
        coEvery {
            repository.getById("WH-999")
        } returns Result.failure(error)

        // When
        val result = useCase("WH-999")

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `when warehouse is missing should return same exception`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException()
        coEvery {
            repository.getById("WH-999")
        } returns Result.failure(error)

        // When
        val result = useCase("WH-999")

        // Then
        assertSame(error, result.exceptionOrNull())
    }


    @Test
    fun `when repository throws exception should return failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Repository failed")

        coEvery {
            repository.getById("WH-001")
        } throws error

        // When
        val result = useCase("WH-001")

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `when repository throws exception should return same exception`() = runBlocking {
        // Given
        val error = IllegalStateException("Repository failed")

        coEvery {
            repository.getById("WH-001")
        } throws error

        // When
        val result = useCase("WH-001")

        // Then
        assertSame(error, result.exceptionOrNull())
    }

}

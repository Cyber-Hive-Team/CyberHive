package org.example.domain.usecase.crud.warehouse

import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.repository.WarehouseRepository
import org.example.domain.validator.*
import org.junit.jupiter.api.Test
import kotlin.test.*

class UpdateWarehouseUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val validator =
        mockk<Validator<Warehouse, UpdateWarehouseInput>>()

    private val useCase = UpdateWarehouseUseCase(repository, validator)

    private val warehouse = Warehouse(
        "WH-001", "Main", RegionalZone.NORTH, 31.5, 34.4
    )

    private val input = UpdateWarehouseInput(
        id = "WH-001",
        name = "Updated"
    )

    @Test
    fun `valid update returns updated warehouse`() = runBlocking {
        // Given
        val updated = Warehouse(
            "WH-001", "Updated", RegionalZone.NORTH, 31.5, 34.4
        )
        coEvery { repository.getById(input.id) } returns
                Result.success(warehouse)
        every { validator.validateUpdate(input) } returns
                ValidationResult.Success
        coEvery { repository.update(input) } returns
                Result.success(updated)

        // When
        val result = useCase(input)

        // Then
        assertSame(updated, result.getOrThrow())
        coVerify(exactly = 1) { repository.update(input) }
    }

    @Test
    fun `missing warehouse prevents update`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException()
        coEvery { repository.getById(input.id) } returns
                Result.failure(error)

        // When
        val result = useCase(input)

        // Then
        assertSame(error, result.exceptionOrNull())
        verify(exactly = 0) { validator.validateUpdate(any()) }
        coVerify(exactly = 0) { repository.update(any()) }
    }

    @Test
    fun `invalid input prevents update`() = runBlocking {
        // Given
        coEvery { repository.getById(input.id) } returns
                Result.success(warehouse)
        every { validator.validateUpdate(input) } returns
                ValidationResult.Failure(
                    listOf(
                        FieldViolation(
                            FieldError.InvalidWarehouseName,
                            "Invalid name"
                        )
                    )
                )

        // When
        val result = useCase(input)

        // Then
        assertIs<EntityValidationException>(result.exceptionOrNull())
        coVerify(exactly = 0) { repository.update(any()) }
    }

    @Test
    fun `repository update failure is returned`() = runBlocking {
        // Given
        val error = IllegalStateException("Update failed")
        coEvery { repository.getById(input.id) } returns
                Result.success(warehouse)
        every { validator.validateUpdate(input) } returns
                ValidationResult.Success
        coEvery { repository.update(input) } returns
                Result.failure(error)

        // When
        val result = useCase(input)

        // Then
        assertSame(error, result.exceptionOrNull())
    }
}

package org.example.domain.usecase.crud.warehouse

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.repository.WarehouseRepository
import org.example.domain.validator.result.ValidationResult
import org.example.domain.validator.result.FieldViolation
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.Validator
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue

class CreateWarehouseUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val validator =
        mockk<Validator<Warehouse, UpdateWarehouseInput>>()

    private val useCase = CreateWarehouseUseCase(repository, validator)

    private val warehouse = Warehouse(
        "WH-001", "Main", RegionalZone.NORTH, 31.5, 34.4
    )

    @Test
    fun `valid warehouse is saved`() = runBlocking {
        // Given
        every { validator.validateCreate(warehouse) } returns
                ValidationResult.Success
        coEvery { repository.save(warehouse) } returns
                Result.success(warehouse)

        // When
        val result = useCase(warehouse)

        // Then
        assertSame(warehouse, result.getOrThrow())
        coVerify(exactly = 1) { repository.save(warehouse) }
    }

    @Test
    fun `validation failure prevents saving`() = runBlocking {
        // Given
        every { validator.validateCreate(warehouse) } returns
                ValidationResult.Failure(
                    listOf(
                        FieldViolation(
                            FieldError.InvalidWarehouseName,
                            "Invalid name"
                        )
                    )
                )

        // When
        val result = useCase(warehouse)

        // Then
        assertIs<EntityValidationException>(result.exceptionOrNull())
        coVerify(exactly = 0) { repository.save(any()) }
    }

    @Test
    fun `save failure is returned`() = runBlocking {
        // Given
        val error = IllegalStateException("Save failed")
        every { validator.validateCreate(warehouse) } returns
                ValidationResult.Success
        coEvery { repository.save(warehouse) } returns
                Result.failure(error)

        // When
        val result = useCase(warehouse)

        // Then
        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }
}

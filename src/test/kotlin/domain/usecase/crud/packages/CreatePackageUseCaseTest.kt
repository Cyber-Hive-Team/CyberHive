package org.example.test.domain.usecase.crud.packages

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Package
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.crud.packages.CreatePackageUseCase
import org.example.domain.validator.result.*
import org.example.domain.validator.Validator
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class CreatePackageUseCaseTest {
    private val factory = TestDataFactory()
    private val repository = mockk<PackageRepository>()
    private val validator = mockk<Validator<Package, UpdatePackageInput>>()

    private val useCase = CreatePackageUseCase(
        packageRepository = repository,
        packageValidator = validator
    )

    @Test
    fun `returns saved package when validation succeeds`() = runBlocking {
        // Given
        val cargoPackage = factory.createPackage()
        every {
            validator.validateCreate(cargoPackage)
        } returns ValidationResult.Success
        coEvery {
            repository.save(cargoPackage)
        } returns Result.success(cargoPackage)

        // When
        val result = useCase(cargoPackage)

        // Then
        assertEquals(cargoPackage, result.getOrThrow())
        coVerify(exactly = 1) {
            repository.save(cargoPackage)
        }
    }

    @Test
    fun `does not save when validation fails`() = runBlocking {
        // Given
        val cargoPackage = factory.createPackage(baseRate = -1.0)

        every {
            validator.validateCreate(cargoPackage)
        } returns ValidationResult.Failure(
            listOf(FieldViolation(FieldError.InvalidBaseRate, "Base rate cannot be negative"))
        )
        // When
        val result = useCase(cargoPackage)

        // Then
        assertInstanceOf(
            EntityValidationException::class.java,
            result.exceptionOrNull()
        )

        coVerify(exactly = 0) {
            repository.save(any())
        }
    }

    @Test
    fun `preserves repository failure`() = runBlocking {
        // Given
        val cargoPackage = factory.createPackage()
        val error = IllegalStateException("Save failed")

        every {
            validator.validateCreate(cargoPackage)
        } returns ValidationResult.Success

        coEvery {
            repository.save(cargoPackage)
        } returns Result.failure(error)

        // When
        val result = useCase(cargoPackage)

        // Then
        assertSame(error, result.exceptionOrNull())
    }


}

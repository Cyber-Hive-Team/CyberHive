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
import org.example.domain.usecase.crud.packages.UpdatePackageUseCase
import org.example.domain.validator.result.ValidationResult
import org.example.domain.validator.result.FieldViolation
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.Validator
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class UpdatePackageUseCaseTest {
    private val factory = TestDataFactory()
    private val repository = mockk<PackageRepository>()
    private val validator = mockk<Validator<Package, UpdatePackageInput>>()

    private val useCase = UpdatePackageUseCase(
        packageRepository = repository,
        packageValidator = validator
    )

    @Test
    fun `returns updated package after successful validation`() = runBlocking {
        // Given
        val updatedPackage = factory.createPackage(weight = 12.0)

        every {
            validator.validateCreate(updatedPackage)
        } returns ValidationResult.Success

        coEvery {
            repository.save(updatedPackage)
        } returns Result.success(updatedPackage)

        // When
        val result = useCase(updatedPackage)

        // Then
        assertEquals(updatedPackage, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.save(updatedPackage)
        }
    }

    @Test
    fun `does not save when validation fails`() = runBlocking {
        // Given
        val updatedPackage = factory.createPackage(baseRate = -1.0)

        every {
            validator.validateCreate(updatedPackage)
        } returns ValidationResult.Failure(
            listOf(
                FieldViolation(
                    FieldError.InvalidBaseRate,
                    "Base rate cannot be negative"
                )
            )
        )

        // When
        val result = useCase(updatedPackage)

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
    fun `preserves save failure`() = runBlocking {
        // Given
        val updatedPackage = factory.createPackage()
        val error = IllegalStateException("Update failed")

        every {
            validator.validateCreate(updatedPackage)
        } returns ValidationResult.Success

        coEvery {
            repository.save(updatedPackage)
        } returns Result.failure(error)

        // When
        val result = useCase(updatedPackage)

        // Then
        assertSame(error, result.exceptionOrNull())
    }

}

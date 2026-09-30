package org.example.test.domain.usecase.crud.packages

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.PackageNotFoundException
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.crud.packages.DeletePackageUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DeletePackageUseCaseTest {
    private val factory = TestDataFactory()
    private val repository = mockk<PackageRepository>()
    private val useCase = DeletePackageUseCase(repository)

    @Test
    fun `returns deletion response when package exists`() = runBlocking {
        // Given
        val cargoPackage = factory.createPackage()
        val expectedResponse = "Package deleted"

        coEvery {
            repository.getById(cargoPackage.id)
        } returns Result.success(cargoPackage)

        coEvery {
            repository.delete(cargoPackage.id)
        } returns Result.success(expectedResponse)

        // When
        val result = useCase(cargoPackage.id)

        // Then
        assertEquals(expectedResponse, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.delete(cargoPackage.id)
        }
    }

    @Test
    fun `does not delete when package is missing`() = runBlocking {
        // Given
        val packageId = "PKG-000999"
        val error = PackageNotFoundException("Package not found")

        coEvery {
            repository.getById(packageId)
        } returns Result.failure(error)

        // When
        val result = useCase(packageId)

        // Then
        assertSame(error, result.exceptionOrNull())

        coVerify(exactly = 0) {
            repository.delete(any())
        }
    }

    @Test
    fun `preserves deletion failure`() = runBlocking {
        // Given
        val cargoPackage = factory.createPackage()
        val error = IllegalStateException("Delete failed")

        coEvery {
            repository.getById(cargoPackage.id)
        } returns Result.success(cargoPackage)

        coEvery {
            repository.delete(cargoPackage.id)
        } returns Result.failure(error)

        // When
        val result = useCase(cargoPackage.id)

        // Then
        assertSame(error, result.exceptionOrNull())
    }

}

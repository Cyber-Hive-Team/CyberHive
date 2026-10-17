package org.example.test.domain.usecase.crud.packages

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.PackageNotFoundException
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.crud.packages.GetPackageUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetPackageUseCaseTest {
    private val factory = TestDataFactory()
    private val repository = mockk<PackageRepository>()
    private val useCase = GetPackageUseCase(repository)

    @Test
    fun `returns requested package`() = runBlocking {
        // Given
        val expectedPackage = factory.createPackage()

        coEvery {
            repository.getById(expectedPackage.id)
        } returns Result.success(expectedPackage)

        // When
        val result = useCase(expectedPackage.id)

        // Then
        assertEquals(expectedPackage, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.getById(expectedPackage.id)
        }
    }

    @Test
    fun `preserves package not found failure`() = runBlocking {
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
    }

    @Test
    fun `preserves repository failure`() = runBlocking {
        // Given
        val packageId = "PKG-000001"
        val error = IllegalStateException("Read failed")

        coEvery {
            repository.getById(packageId)
        } returns Result.failure(error)

        // When
        val result = useCase(packageId)

        // Then
        assertSame(error, result.exceptionOrNull())
    }

}

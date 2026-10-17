package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.FindPackagesByPriorityUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FindPackagesByPriorityUseCaseTest {
    private val factory = TestDataFactory()
    private val repository = mockk<PackageRepository>()
    private val useCase = FindPackagesByPriorityUseCase(repository)

    @Test
    fun `returns only packages with requested priority`() = runBlocking {
        // Given
        val firstUrgent = factory.createPackage(id = "PKG-000001", priority = Priority.URGENT)
        val standard = factory.createPackage(id = "PKG-000002", priority = Priority.STANDARD)
        val secondUrgent = factory.createPackage(id = "PKG-000003", priority = Priority.URGENT)
        coEvery {
            repository.getAllPackages()
        } returns Result.success(
            listOf(firstUrgent, standard, secondUrgent)
        )

        // When
        val result = useCase(Priority.URGENT)

        // Then
        assertEquals(
            listOf(firstUrgent, secondUrgent),
            result.getOrThrow()
        )

        coVerify(exactly = 1) {
            repository.getAllPackages()
        }
    }

    @Test
    fun `returns empty list when no priority matches`() = runBlocking {
        // Given
        val standard = factory.createPackage(id = "PKG-000001", priority = Priority.STANDARD)

        coEvery {
            repository.getAllPackages()
        } returns Result.success(listOf(standard))

        // When
        val result = useCase(Priority.URGENT)

        // Then
        assertEquals(emptyList<Package>(), result.getOrThrow())
    }

    @Test
    fun `returns empty list when there are no packages`() = runBlocking {
        // Given
        coEvery {
            repository.getAllPackages()
        } returns Result.success(emptyList())

        // When
        val result = useCase(Priority.URGENT)

        // Then
        assertEquals(emptyList<Package>(), result.getOrThrow())
    }

    @Test
    fun `preserves repository failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Read failed")

        coEvery {
            repository.getAllPackages()
        } returns Result.failure(error)

        // When
        val result = useCase(Priority.URGENT)

        // Then
        assertSame(error, result.exceptionOrNull())
    }

}

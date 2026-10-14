package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.example.domain.algorithm.knn.KnnSectorClassifier
import org.example.domain.model.Package
import org.example.domain.model.PackageSectorInput
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.repository.PackageRepository
import org.example.domain.usecase.ClassifyPackageSectorUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ClassifyPackageSectorUseCaseTest {

    private val repository = mockk<PackageRepository>()
    private val classifier = mockk<KnnSectorClassifier>()

    private val useCase = ClassifyPackageSectorUseCase(
        repository,
        classifier
    )

    private val input = PackageSectorInput(
        packageId = "PKG-000099",
        latitude = 32.0,
        longitude = 35.0,
        weight = 10.0
    )

    private fun cargoPackage(
        id: String,
        zone: RegionalZone
    ): Package {
        val warehouse = Warehouse(
            id = "WH-001",
            name = "Destination",
            regionalZone = zone,
            latitude = 32.0,
            longitude = 35.0
        )

        val origin = Warehouse(
            id = "WH-002",
            name = "Origin",
            regionalZone = RegionalZone.CENTRAL,
            latitude = 31.0,
            longitude = 34.0
        )

        return Package(
            id = id,
            weight = 10.0,
            priority = Priority.STANDARD,
            originWarehouse = origin,
            destinationWarehouse = warehouse
        )
    }

    @Test
    fun `returns predicted zone successfully`() = runBlocking {
        // Given
        val packages = listOf(
            cargoPackage("PKG-000001", RegionalZone.NORTH)
        )

        coEvery {
            repository.getAllPackages()
        } returns Result.success(packages)

        every {
            classifier.classify(input, packages)
        } returns RegionalZone.NORTH

        // When
        val result = useCase(input)

        // Then
        assertEquals(RegionalZone.NORTH, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.getAllPackages()
        }

        verify(exactly = 1) {
            classifier.classify(input, packages)
        }
    }

    @Test
    fun `returns failure when repository fails`() = runBlocking {
        // Given
        coEvery {
            repository.getAllPackages()
        } returns Result.failure(
            IllegalStateException("Unable to load packages")
        )

        // When
        val result = useCase(input)

        // Then
        assertTrue(result.isFailure)

        coVerify(exactly = 1) {
            repository.getAllPackages()
        }

        verify(exactly = 0) {
            classifier.classify(any(), any())
        }
    }

    @Test
    fun `returns failure when classifier throws`() = runBlocking {
        // Given
        val packages = listOf(
            cargoPackage("PKG-000002", RegionalZone.NORTH)
        )

        coEvery {
            repository.getAllPackages()
        } returns Result.success(packages)

        every {
            classifier.classify(input, packages)
        } throws IllegalArgumentException("Invalid training data")

        // When
        val result = useCase(input)

        // Then
        assertTrue(result.isFailure)

        verify(exactly = 1) {
            classifier.classify(input, packages)
        }
    }
}

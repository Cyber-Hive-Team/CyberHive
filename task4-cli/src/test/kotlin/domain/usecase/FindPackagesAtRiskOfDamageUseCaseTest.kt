package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.PackageRequirements
import org.example.domain.model.WarehouseServices
import org.example.domain.model.result.DamageRiskResult
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.FindPackagesAtRiskOfDamageUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FindPackagesAtRiskOfDamageUseCaseTest {
    private val factory = TestDataFactory()
    private val packageRepository = mockk<PackageRepository>()
    private val warehouseRepository = mockk<WarehouseRepository>()

    private val useCase = FindPackagesAtRiskOfDamageUseCase(
        packageRepository,
        warehouseRepository
    )

    private val cargoPackage = factory.createPackage()

    @Test
    fun `detects missing fragile handling`() = runBlocking {
        // Given
        prepare(
            requirements = createRequirements(fragile = true),
            services = createServices(fragile = false)
        )

        // When
        val result = useCase()

        // Then
        assertEquals(
            listOf(expectedRisk("Fragile handling unavailable")),
            result.getOrThrow()
        )
    }

    @Test
    fun `detects missing cold storage`() = runBlocking {
        // Given
        prepare(
            requirements = createRequirements(cold = true),
            services = createServices(cold = false)
        )

        // When
        val result = useCase()

        // Then
        assertEquals(
            listOf(expectedRisk("Cold storage unavailable")),
            result.getOrThrow()
        )
    }

    @Test
    fun `detects missing special handling`() = runBlocking {
        // Given
        prepare(
            requirements = createRequirements(special = true),
            services = createServices(special = false)
        )

        // When
        val result = useCase()

        // Then
        assertEquals(
            listOf(expectedRisk("Special handling unavailable")),
            result.getOrThrow()
        )
    }

    @Test
    fun `returns no risk when all required services exist`() = runBlocking {
        // Given
        prepare(
            requirements = createRequirements(
                fragile = true,
                cold = true,
                special = true
            ),
            services = createServices()
        )

        // When
        val result = useCase()

        // Then
        assertEquals(
            emptyList<DamageRiskResult>(),
            result.getOrThrow()
        )
    }

    @Test
    fun `preserves requirements loading failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Requirements failed")

        coEvery {
            packageRepository.getAllPackageRequirements()
        } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertSame(error, result.exceptionOrNull())

        coVerify(exactly = 0) {
            warehouseRepository.getAllWarehouseServices()
        }
    }

    @Test
    fun `preserves services loading failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Services failed")

        coEvery {
            packageRepository.getAllPackageRequirements()
        } returns Result.success(listOf(createRequirements()))

        coEvery {
            warehouseRepository.getAllWarehouseServices()
        } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertSame(error, result.exceptionOrNull())

        coVerify(exactly = 0) {
            packageRepository.getAllPackages()
        }
    }

    @Test
    fun `preserves packages loading failure`() = runBlocking {
        // Given
        prepare(createRequirements(), createServices())
        val error = IllegalStateException("Packages failed")

        coEvery {
            packageRepository.getAllPackages()
        } returns Result.failure(error)

        // When
        val result = useCase()

        // Then
        assertSame(error, result.exceptionOrNull())
    }

    private fun prepare(
        requirements: PackageRequirements,
        services: WarehouseServices
    ) {
        coEvery {
            packageRepository.getAllPackageRequirements()
        } returns Result.success(listOf(requirements))

        coEvery {
            warehouseRepository.getAllWarehouseServices()
        } returns Result.success(listOf(services))

        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(listOf(cargoPackage))
    }

    private fun createRequirements(
        fragile: Boolean = false,
        cold: Boolean = false,
        special: Boolean = false
    ): PackageRequirements = PackageRequirements(
        packageId = cargoPackage.id,
        isFragile = fragile,
        requiresColdStorage = cold,
        requiresSpecialHandling = special
    )

    private fun createServices(
        fragile: Boolean = true,
        cold: Boolean = true,
        special: Boolean = true
    ): WarehouseServices = WarehouseServices(
        warehouseId = cargoPackage.originWarehouse.id,
        supportsFragileHandling = fragile,
        supportsColdStorage = cold,
        supportsSpecialHandling = special
    )

    private fun expectedRisk(reason: String): DamageRiskResult =
        DamageRiskResult(
            packageId = cargoPackage.id,
            warehouseId = cargoPackage.originWarehouse.id,
            reason = reason
        )


}

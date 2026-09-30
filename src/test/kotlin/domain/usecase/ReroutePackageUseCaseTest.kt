package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.example.domain.algorithm.search.Router
import org.example.domain.model.Package
import org.example.domain.model.exception.PackageNotFoundException
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.input.ReroutePackageInput
import org.example.domain.model.result.RoutingResult
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.ReroutePackageUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class ReroutePackageUseCaseTest {

    private val factory = TestDataFactory()
    private val packageRepository = mockk<PackageRepository>()
    private val warehouseRepository = mockk<WarehouseRepository>()
    private val router = mockk<Router>()
    private val pricingEngine = mockk<RoutePricingEngine>()
    private val useCase = ReroutePackageUseCase(packageRepository, warehouseRepository, router, pricingEngine)
    private val cargoPackage = factory.createPackage()
    private val destination = factory.createWarehouse("WH-003")
    private val input = ReroutePackageInput(packageId = cargoPackage.id, newDestinationWarehouseId = destination.id)
    private val routingResult =
        RoutingResult(path = listOf(cargoPackage.originWarehouse, destination), distanceKm = 75.0)

    @Test
    fun `reroutes package and adds updated package to destination queue`() = runBlocking {
        // Given
        prepareExistingPackageAndDestination()
        every {
            router.findPath(cargoPackage.originWarehouse, destination)
        } returns routingResult
        every {
            pricingEngine.calculatePrice(cargoPackage, routingResult.distanceKm)
        } returns 150.0
        val savedPackage = slot<Package>()
        coEvery {
            warehouseRepository.addPackageToCargoQueue(destination.id, capture(savedPackage))
        } returns Result.success(true)
        coEvery {
            warehouseRepository.sortCargoQueue(destination.id)
        } returns Result.success(true)
        // When
        val result = useCase(input)
        // Then
        assertEquals(routingResult, result.getOrThrow())

        assertEquals(
            cargoPackage.copy(destinationWarehouse = destination, baseRate = 150.0),
            savedPackage.captured
        )
        verify(exactly = 1) {
            pricingEngine.calculatePrice(cargoPackage, routingResult.distanceKm)
        }
        coVerify(exactly = 1) {
            warehouseRepository.sortCargoQueue(destination.id)
        }
    }

    @Test
    fun `fails when package is missing`() = runBlocking {
        // Given
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(emptyList())
        // When
        val result = useCase(input)
        // Then
        assertInstanceOf(PackageNotFoundException::class.java, result.exceptionOrNull())
        coVerify(exactly = 0) {
            warehouseRepository.getById(any())
        }
    }

    @Test
    fun `preserves destination lookup failure`() = runBlocking {
        // Given
        val error = WarehouseNotFoundException("Destination not found")
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(listOf(cargoPackage))
        coEvery {
            warehouseRepository.getById(destination.id)
        } returns Result.failure(error)
        // When
        val result = useCase(input)
        // Then
        assertSame(error, result.exceptionOrNull())
        verify(exactly = 0) {
            router.findPath(any(), any())
        }
    }

    @Test
    fun `fails when no route exists`() = runBlocking {
        // Given
        prepareExistingPackageAndDestination()

        every {
            router.findPath(cargoPackage.originWarehouse, destination)
        } returns RoutingResult(path = emptyList(), distanceKm = 0.0)
        // When
        val result = useCase(input)
        // Then
        assertInstanceOf(RouteNotFoundException::class.java, result.exceptionOrNull())
        coVerify(exactly = 0) {
            warehouseRepository.addPackageToCargoQueue(any(), any())
        }
    }

    @Test
    fun `preserves package loading failure`() = runBlocking {
        // Given
        val error = IllegalStateException("Packages failed")
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.failure(error)
        // When
        val result = useCase(input)
        // Then
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `preserves queue insertion failure without sorting`() = runBlocking {
        // Given
        prepareExistingPackageAndDestination()
        every {
            router.findPath(cargoPackage.originWarehouse, destination)
        } returns routingResult
        every {
            pricingEngine.calculatePrice(cargoPackage, any())
        } returns 150.0
        val error = IllegalStateException("Queue insertion failed")
        coEvery {
            warehouseRepository.addPackageToCargoQueue(destination.id, any())
        } returns Result.failure(error)
        // When
        val result = useCase(input)
        // Then
        assertSame(error, result.exceptionOrNull())
        coVerify(exactly = 0) {
            warehouseRepository.sortCargoQueue(any())
        }
    }

    private fun prepareExistingPackageAndDestination() {
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(listOf(cargoPackage))
        coEvery {
            warehouseRepository.getById(destination.id)
        } returns Result.success(destination)
    }


}

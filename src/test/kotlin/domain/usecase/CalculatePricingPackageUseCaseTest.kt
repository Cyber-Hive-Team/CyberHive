package org.example.test.domain.usecase

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.example.domain.model.exception.PackageNotFoundException
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.model.input.CalculatePricingInput
import org.example.domain.model.result.PricingPackage
import org.example.domain.pricing.DispatchStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository
import org.example.domain.usecase.CalculatePricingPackageUseCase
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CalculatePricingPackageUseCaseTest {
    private val factory = TestDataFactory()
    private val packageRepository = mockk<PackageRepository>()
    private val routeRepository = mockk<RouteRepository>()
    private val pricingEngine = mockk<RoutePricingEngine>()

    private val useCase = CalculatePricingPackageUseCase(
        packageRepository,
        routeRepository,
        pricingEngine
    )

    private val cargoPackage = factory.createPackage()
    private val route = factory.createRoute(cargoPackage)

    @Test
    fun `returns price calculated by pricing engine`() = runBlocking {
        // Given
        prepareRepositories()

        every {
            pricingEngine.calculatePrice(cargoPackage, route.distanceKm)
        } returns 125.0

        // When
        val result = useCase(
            CalculatePricingInput(
                packageId = cargoPackage.id,
                routeId = route.id
            )
        )

        // Then
        assertEquals(PricingPackage(125.0), result)

        verify(exactly = 1) {
            pricingEngine.calculatePrice(cargoPackage, route.distanceKm)
        }

        verify(exactly = 0) {
            pricingEngine.setStrategy(any())
        }
    }

    @Test
    fun `sets custom strategy before calculating price`() = runBlocking {
        // Given
        prepareRepositories()
        val strategy = mockk<DispatchStrategy>()

        every {
            pricingEngine.setStrategy(strategy)
        } just Runs

        every {
            pricingEngine.calculatePrice(cargoPackage, route.distanceKm)
        } returns 90.0

        // When
        val result = useCase(
            CalculatePricingInput(
                packageId = cargoPackage.id,
                routeId = route.id,
                customStrategy = strategy
            )
        )

        // Then
        assertEquals(PricingPackage(90.0), result)

        verifyOrder {
            pricingEngine.setStrategy(strategy)
            pricingEngine.calculatePrice(cargoPackage, route.distanceKm)
        }
    }

    @Test
    fun `throws when requested package is missing`() {
        // Given
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(emptyList())

        // When / Then
        assertThrows(PackageNotFoundException::class.java) {
            runBlocking {
                useCase(
                    CalculatePricingInput(
                        packageId = "PKG-000999",
                        routeId = route.id
                    )
                )
            }
        }

        coVerify(exactly = 0) {
            routeRepository.getAllRoutes()
        }

        verify(exactly = 0) {
            pricingEngine.calculatePrice(any(), any())
        }
    }

    @Test
    fun `throws when requested route is missing`() {
        // Given
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(listOf(cargoPackage))

        coEvery {
            routeRepository.getAllRoutes()
        } returns Result.success(emptyList())

        // When / Then
        assertThrows(RouteNotFoundException::class.java) {
            runBlocking {
                useCase(
                    CalculatePricingInput(
                        packageId = cargoPackage.id,
                        routeId = "RT-00999"
                    )
                )
            }
        }

        verify(exactly = 0) {
            pricingEngine.calculatePrice(any(), any())
        }
    }

    @Test
    fun `propagates package repository exception`() {
        // Given
        val error = IllegalStateException("Packages failed")

        coEvery {
            packageRepository.getAllPackages()
        } returns Result.failure(error)

        // When
        val actual = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                useCase(CalculatePricingInput(cargoPackage.id, route.id))
            }
        }

        // Then
        assertSame(error, actual)

        coVerify(exactly = 0) {
            routeRepository.getAllRoutes()
        }
    }

    @Test
    fun `propagates route repository exception`() {
        // Given
        val error = IllegalStateException("Routes failed")

        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(listOf(cargoPackage))

        coEvery {
            routeRepository.getAllRoutes()
        } returns Result.failure(error)

        // When
        val actual = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                useCase(CalculatePricingInput(cargoPackage.id, route.id))
            }
        }

        // Then
        assertSame(error, actual)

        verify(exactly = 0) {
            pricingEngine.calculatePrice(any(), any())
        }
    }

    private fun prepareRepositories() {
        coEvery {
            packageRepository.getAllPackages()
        } returns Result.success(listOf(cargoPackage))

        coEvery {
            routeRepository.getAllRoutes()
        } returns Result.success(listOf(route))
    }


}

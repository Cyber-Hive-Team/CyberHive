package org.example.test.presentation

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.repository.WarehouseRepository
import org.example.presentation.DataLoader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DataLoaderConcurrencyTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val packageRepository = mockk<PackageRepository>()
    private val vehicleRepository = mockk<VehicleRepository>()
    private val routeRepository = mockk<RouteRepository>()

    private val dataLoader = DataLoader(
        warehouseRepository = warehouseRepository,
        packageRepository = packageRepository,
        vehicleRepository = vehicleRepository,
        routeRepository = routeRepository
    )

    @Test
    fun `should start all repository loads before any load completes`() = runTest {
        // Given
        val releaseLoading = CompletableDeferred<Unit>()
        val startedRepositories = mutableSetOf<String>()

        mockConcurrentRepositoryLoads(releaseLoading, startedRepositories)

        // When
        val loading = async { dataLoader.load() }
        testScheduler.runCurrent()

        val actualStartedRepositories = startedRepositories.toSet()

        releaseLoading.complete(Unit)
        loading.await()

        // Then
        assertEquals(
            setOf("warehouses", "packages", "vehicles", "routes"),
            actualStartedRepositories
        )
    }

    @Test
    fun `should propagate repository failure to caller`() = runTest {
        // Given
        val expectedMessage = "Loading failed"

        coEvery { warehouseRepository.getAllWarehouses() } returns
                Result.failure(IllegalStateException(expectedMessage))

        coEvery { packageRepository.getAllPackages() } returns
                Result.success(emptyList())

        coEvery { vehicleRepository.getVehicles() } returns
                Result.success(emptyList())

        coEvery { routeRepository.getAllRoutes() } returns
                Result.success(emptyList())

        // When
        val actualFailure = runCatching {
            dataLoader.load()
        }.exceptionOrNull()

        // Then
        assertEquals(expectedMessage, actualFailure?.message)
    }

    @Test
    fun `should cancel active sibling load when repository fails`() = runTest {
        // Given
        val packageStarted = CompletableDeferred<Unit>()
        val packageCancelled = CompletableDeferred<Unit>()
        val releaseFailure = CompletableDeferred<Unit>()

        mockRepositoryFailure(packageStarted, packageCancelled, releaseFailure)
        val testScope = CoroutineScope(
            SupervisorJob() + StandardTestDispatcher(testScheduler))

        try {
            // When
            val loading = testScope.async { dataLoader.load() }
            testScheduler.runCurrent()
            packageStarted.await()
            releaseFailure.complete(Unit)
            testScheduler.runCurrent()
            runCatching { loading.await() }

            // Then
            assertTrue(packageCancelled.isCompleted) }
        finally { testScope.cancel() }
    }


    private fun mockConcurrentRepositoryLoads(
        releaseLoading: CompletableDeferred<Unit>,
        startedRepositories: MutableSet<String>
    ) {
        coEvery { warehouseRepository.getAllWarehouses() } coAnswers {
            startedRepositories.add("warehouses")
            releaseLoading.await()
            Result.success(emptyList())
        }

        coEvery { packageRepository.getAllPackages() } coAnswers {
            startedRepositories.add("packages")
            releaseLoading.await()
            Result.success(emptyList())
        }

        coEvery { vehicleRepository.getVehicles() } coAnswers {
            startedRepositories.add("vehicles")
            releaseLoading.await()
            Result.success(emptyList())
        }

        coEvery { routeRepository.getAllRoutes() } coAnswers {
            startedRepositories.add("routes")
            releaseLoading.await()
            Result.success(emptyList())
        }
    }

    private fun mockRepositoryFailure(
        packageStarted: CompletableDeferred<Unit>,
        packageCancelled: CompletableDeferred<Unit>,
        releaseFailure: CompletableDeferred<Unit>
    ) {
        coEvery { warehouseRepository.getAllWarehouses() } coAnswers {
            releaseFailure.await()
            Result.failure(IllegalStateException("Loading failed"))
        }

        coEvery { packageRepository.getAllPackages() } coAnswers {
            try {
                packageStarted.complete(Unit)
                awaitCancellation()
            } finally {
                packageCancelled.complete(Unit)
            }
        }

        coEvery { vehicleRepository.getVehicles() } coAnswers {
            awaitCancellation()
        }

        coEvery { routeRepository.getAllRoutes() } coAnswers {
            awaitCancellation()
        }
    }
}

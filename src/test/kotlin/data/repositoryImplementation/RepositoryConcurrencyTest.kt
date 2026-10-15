package org.example.test.data.repositoryImplementation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.csv.CsvWarehouseStatusDataSource
import org.example.data.datasource.remote.PackageRemoteDataSource
import org.example.data.datasource.remote.RouteRemoteDataSource
import org.example.data.datasource.remote.VehicleRemoteDataSource
import org.example.data.datasource.remote.WarehouseRemoteDataSource
import org.example.data.repositoryImplementation.PackageRepositoryImpl
import org.example.data.repositoryImplementation.RouteRepositoryImpl
import org.example.data.repositoryImplementation.VehicleRepositoryImpl
import org.example.data.repositoryImplementation.WarehouseRepositoryImpl
import org.example.data.telemetry.TelemetryStreamingService
import org.junit.jupiter.api.Test

class RepositoryConcurrencyTest {

    @Test
    fun `should load warehouses once when requests are concurrent`() = runTest {
        // Given
        val local = mockk<WarehouseDataSource>()
        val release = CompletableDeferred<Unit>()
        coEvery { local.getWarehouses() } coAnswers {
            release.await()
            emptyList()
        }
        val repository = WarehouseRepositoryImpl(
            mockk<WarehouseRemoteDataSource>(),
            local,
            mockk<CsvWarehouseStatusDataSource>()
        )

        // When
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getAllWarehouses()
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getAllWarehouses()
        }
        release.complete(Unit)
        first.await().getOrThrow()
        second.await().getOrThrow()

        // Then
        coVerify(exactly = 1) { local.getWarehouses() }
    }

    @Test
    fun `should load vehicles once when requests are concurrent`() = runTest {
        // Given
        val local = mockk<VehicleDataSource>()
        val release = CompletableDeferred<Unit>()
        coEvery { local.getVehicles() } coAnswers {
            release.await()
            emptyList()
        }
        val repository = VehicleRepositoryImpl(
            mockk<VehicleRemoteDataSource>(),
            local,
            telemetryStreamingService = mockk<TelemetryStreamingService>()


        )

        // When
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getVehicles()
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getVehicles()
        }
        release.complete(Unit)
        first.await().getOrThrow()
        second.await().getOrThrow()

        // Then
        coVerify(exactly = 1) { local.getVehicles() }
    }

    @Test
    fun `should load packages once when requests are concurrent`() = runTest {
        // Given
        val local = mockk<PackageDataSource>()
        val release = CompletableDeferred<Unit>()
        coEvery { local.getPackages() } coAnswers {
            release.await()
            emptyList()
        }
        val repository = PackageRepositoryImpl(
            mockk<PackageRemoteDataSource>(),
            local
        )

        // When
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getAllPackages()
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getAllPackages()
        }
        release.complete(Unit)
        first.await().getOrThrow()
        second.await().getOrThrow()

        // Then
        coVerify(exactly = 1) { local.getPackages() }
    }

    @Test
    fun `should load routes once when requests are concurrent`() = runTest {
        // Given
        val local = mockk<RouteDataSource>()
        val release = CompletableDeferred<Unit>()
        coEvery { local.getRoutes() } coAnswers {
            release.await()
            emptyList()
        }
        val repository = RouteRepositoryImpl(
            local,
            mockk<RouteRemoteDataSource>()
        )

        // When
        val first = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getAllRoutes()
        }
        val second = async(start = CoroutineStart.UNDISPATCHED) {
            repository.getAllRoutes()
        }
        release.complete(Unit)
        first.await().getOrThrow()
        second.await().getOrThrow()

        // Then
        coVerify(exactly = 1) { local.getRoutes() }
    }

    @Test
    fun `should retry loading when first attempt fails`() = runTest {
        // Given
        val local = mockk<RouteDataSource>()
        coEvery { local.getRoutes() } throws
                IllegalStateException("CSV unavailable")
        val repository = RouteRepositoryImpl(
            local,
            mockk<RouteRemoteDataSource>()
        )

        // When
        repository.getAllRoutes()
        coEvery { local.getRoutes() } returns emptyList()
        repository.getAllRoutes().getOrThrow()

        // Then
        coVerify(exactly = 2) { local.getRoutes() }
    }
}

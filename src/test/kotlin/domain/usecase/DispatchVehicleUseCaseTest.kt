package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.DispatchVehicleUseCase
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DispatchVehicleUseCaseTest {

    private val vehicleRepository = mockk<VehicleRepository>()
    private val packageRepository = mockk<PackageRepository>()

    private val useCase = DispatchVehicleUseCase(
        vehicleRepository,
        packageRepository
    )

    private val warehouse1 = Warehouse(
        "WH-001",
        "Warehouse 1",
        RegionalZone.NORTH,
        31.5,
        34.4
    )

    private val warehouse2 = Warehouse(
        "WH-002",
        "Warehouse 2",
        RegionalZone.SOUTH,
        31.6,
        34.5
    )

    private val vehicle = Vehicle(
        id = "TRK-0001",
        maxCapacityKg = 100.0,
        costPerKm = 2.0,
        currentHub = warehouse1
    )

    private val package1 = Package(
        id = "PKG-000001",
        weight = 20.0,
        priority = Priority.STANDARD,
        originWarehouse = warehouse1,
        destinationWarehouse = warehouse2
    )

    private val package2 = Package(
        id = "PKG-000002",
        weight = 30.0,
        priority = Priority.URGENT,
        originWarehouse = warehouse1,
        destinationWarehouse = warehouse2
    )

    private val packageFromAnotherWarehouse = Package(
        id = "PKG-000003",
        weight = 10.0,
        priority = Priority.LOW,
        originWarehouse = warehouse2,
        destinationWarehouse = warehouse1
    )

    @Test
    fun `loads available packages for vehicle successfully`() {
        runBlocking {

            coEvery {
                vehicleRepository.getVehicles()
            } returns Result.success(listOf(vehicle))

            coEvery {
                packageRepository.getAllPackages()
            } returns Result.success(
                listOf(
                    package1,
                    package2,
                    packageFromAnotherWarehouse
                )
            )

            val result = useCase("TRK-0001")

            assertTrue(result.isSuccess)

            assertEquals(
                listOf(package1, package2),
                result.getOrThrow()
            )
        }
    }

    @Test
    fun `returns failure when vehicle does not exist`() {
        runBlocking {

            coEvery {
                vehicleRepository.getVehicles()
            } returns Result.success(emptyList())

            val result = useCase("TRK-9999")

            assertTrue(result.isFailure)

            assertTrue(
                result.exceptionOrNull() is VehicleNotFoundException
            )
        }
    }

    @Test
    fun `returns failure when getting vehicles fails`() {
        runBlocking {

            val error = IllegalStateException(
                "Failed to get vehicles"
            )

            coEvery {
                vehicleRepository.getVehicles()
            } returns Result.failure(error)

            val result = useCase("TRK-0001")

            assertTrue(result.isFailure)

            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `returns failure when getting packages fails`() {
        runBlocking {

            val error = IllegalStateException(
                "Failed to get packages"
            )

            coEvery {
                vehicleRepository.getVehicles()
            } returns Result.success(listOf(vehicle))

            coEvery {
                packageRepository.getAllPackages()
            } returns Result.failure(error)

            val result = useCase("TRK-0001")

            assertTrue(result.isFailure)

            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `does not load packages that exceed vehicle capacity`() {
        runBlocking {

            val smallVehicle = Vehicle(
                id = "TRK-0002",
                maxCapacityKg = 40.0,
                costPerKm = 2.0,
                currentHub = warehouse1
            )

            coEvery {
                vehicleRepository.getVehicles()
            } returns Result.success(listOf(smallVehicle))

            coEvery {
                packageRepository.getAllPackages()
            } returns Result.success(
                listOf(
                    package1,
                    package2
                )
            )

            val result = useCase("TRK-0002")

            assertTrue(result.isSuccess)

            assertEquals(
                listOf(package1),
                result.getOrThrow()
            )
        }
    }

    @Test
    fun `returns empty list when no packages are available at vehicle hub`() {
        runBlocking {

            coEvery {
                vehicleRepository.getVehicles()
            } returns Result.success(listOf(vehicle))

            coEvery {
                packageRepository.getAllPackages()
            } returns Result.success(
                listOf(packageFromAnotherWarehouse)
            )

            val result = useCase("TRK-0001")

            assertTrue(result.isSuccess)

            assertEquals(
                emptyList(),
                result.getOrThrow()
            )
        }
    }
}

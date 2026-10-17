package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.algorithm.greedy.GreedyFleetDispatcher
import org.example.domain.model.RegionalZone
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.DispatchFleetGreedyUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DispatchFleetGreedyUseCaseTest {

    private val vehicleRepository = mockk<VehicleRepository>()
    private val dispatcher = mockk<GreedyFleetDispatcher>()

    private val useCase = DispatchFleetGreedyUseCase(
        vehicleRepository = vehicleRepository,
        dispatcher = dispatcher
    )

    private val northWarehouse = Warehouse(
        id = "WH-001",
        name = "North Warehouse",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.4
    )

    private val southWarehouse = Warehouse(
        id = "WH-002",
        name = "South Warehouse",
        regionalZone = RegionalZone.SOUTH,
        latitude = 31.3,
        longitude = 34.5
    )

    private val northVehicle = Vehicle(
        id = "TRK-0001",
        maxCapacityKg = 100.0,
        costPerKm = 2.0,
        currentHub = northWarehouse
    )

    private val southVehicle = Vehicle(
        id = "TRK-0002",
        maxCapacityKg = 150.0,
        costPerKm = 2.5,
        currentHub = southWarehouse
    )

    private val availableVehicles = listOf(
        northVehicle,
        southVehicle
    )

    private val targetZones = setOf(
        RegionalZone.NORTH,
        RegionalZone.SOUTH
    )

    @Test
    fun `returns successfully dispatched vehicles`() = runBlocking {

        // Given
        val dispatchedVehicles = listOf(
            northVehicle,
            southVehicle
        )

        coEvery {
            vehicleRepository.getVehicles()
        } returns Result.success(availableVehicles)

        every {
            dispatcher.dispatch(
                targetZones = targetZones,
                availableVehicles = availableVehicles
            )
        } returns dispatchedVehicles

        // When
        val result = useCase(targetZones)

        // Then
        assertEquals(
            dispatchedVehicles,
            result.getOrThrow()
        )

        coVerify(exactly = 1) {
            vehicleRepository.getVehicles()
        }

        coVerify(exactly = 1) {
            dispatcher.dispatch(
                targetZones = targetZones,
                availableVehicles = availableVehicles
            )
        }
    }

    @Test
    fun `returns VehicleNotFoundException when no vehicles are available`() = runBlocking {

        // Given
        coEvery {
            vehicleRepository.getVehicles()
        } returns Result.success(emptyList())

        // When
        val result = useCase(targetZones)

        // Then
        assertEquals(
            VehicleNotFoundException::class,
            result.exceptionOrNull()!!::class
        )

        coVerify(exactly = 1) {
            vehicleRepository.getVehicles()
        }

        coVerify(exactly = 0) {
            dispatcher.dispatch(any(), any())
        }
    }

    @Test
    fun `returns repository failure when getting vehicles fails`() = runBlocking {

        // Given
        val error = IllegalStateException(
            "Failed to get vehicles"
        )

        coEvery {
            vehicleRepository.getVehicles()
        } returns Result.failure(error)

        // When
        val result = useCase(targetZones)

        // Then
        assertSame(
            error,
            result.exceptionOrNull()
        )

        coVerify(exactly = 1) {
            vehicleRepository.getVehicles()
        }

        coVerify(exactly = 0) {
            dispatcher.dispatch(any(), any())
        }
    }

    @Test
    fun `returns failure when greedy dispatcher fails`() = runBlocking {

        // Given
        val error = IllegalStateException(
            "Greedy dispatch failed"
        )

        coEvery {
            vehicleRepository.getVehicles()
        } returns Result.success(availableVehicles)

        every {
            dispatcher.dispatch(
                targetZones = targetZones,
                availableVehicles = availableVehicles
            )
        } throws error

        // When
        val result = useCase(targetZones)

        // Then
        assertSame(
            error,
            result.exceptionOrNull()
        )

        coVerify(exactly = 1) {
            vehicleRepository.getVehicles()
        }

        coVerify(exactly = 1) {
            dispatcher.dispatch(
                targetZones = targetZones,
                availableVehicles = availableVehicles
            )
        }
    }
}

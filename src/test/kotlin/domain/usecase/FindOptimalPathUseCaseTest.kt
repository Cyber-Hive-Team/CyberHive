package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.result.RoutingResult
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.FindOptimalPathUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class FindOptimalPathUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val routeRepository = mockk<RouteRepository>()

    private val useCase = FindOptimalPathUseCase(
        warehouseRepository,
        routeRepository
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
        RegionalZone.NORTH,
        31.6,
        34.5
    )

    private val warehouse3 = Warehouse(
        "WH-003",
        "Warehouse 3",
        RegionalZone.SOUTH,
        31.3,
        34.6
    )

    private val warehouses = listOf(
        warehouse1,
        warehouse2,
        warehouse3
    )

    private val routes = listOf(
        Route(
            "RT-00001",
            10.0,
            15,
            warehouse1,
            warehouse2
        ),
        Route(
            "RT-00002",
            5.0,
            10,
            warehouse2,
            warehouse3
        ),
        Route(
            "RT-00003",
            30.0,
            20,
            warehouse1,
            warehouse3
        )
    )

    private fun stubWarehouses() {
        coEvery {
            warehouseRepository.getAllWarehouses()
        } returns Result.success(warehouses)
    }

    private fun stubRoutes() {
        coEvery {
            routeRepository.getAllRoutes()
        } returns Result.success(routes)
    }

    private fun stubRoutesFailure(error: Throwable) {
        coEvery {
            routeRepository.getAllRoutes()
        } returns Result.failure(error)
    }

    @Test
    fun `finds optimal path successfully`() = runBlocking {
        // Given
        stubWarehouses()
        stubRoutes()

        // When
        val result = useCase(
            "WH-001",
            "WH-003"
        )

        // Then
        assertEquals(
            RoutingResult(
                path = listOf(
                    warehouse1,
                    warehouse2,
                    warehouse3
                ),
                distanceKm = 15.0
            ),
            result
        )

        coVerify(exactly = 1) {
            warehouseRepository.getAllWarehouses()
        }

        coVerify(exactly = 1) {
            routeRepository.getAllRoutes()
        }
    }

    @Test
    fun `throws exception when start warehouse does not exist`() =
        runBlocking {
            // Given
            stubWarehouses()

            // When
            val exception = try {
                useCase("WH-999", "WH-003")
                null
            } catch (e: WarehouseNotFoundException) {
                e
            }

            // Then
            assertEquals(
                WarehouseNotFoundException::class,
                exception!!::class
            )

            coVerify(exactly = 1) {
                warehouseRepository.getAllWarehouses()
            }

            coVerify(exactly = 0) {
                routeRepository.getAllRoutes()
            }
        }

    @Test
    fun `throws exception when destination warehouse does not exist`() =
        runBlocking {
            // Given
            stubWarehouses()

            // When
            val exception = try {
                useCase("WH-001", "WH-999")
                null
            } catch (e: WarehouseNotFoundException) {
                e
            }

            // Then
            assertEquals(
                WarehouseNotFoundException::class,
                exception!!::class
            )

            coVerify(exactly = 1) {
                warehouseRepository.getAllWarehouses()
            }

            coVerify(exactly = 0) {
                routeRepository.getAllRoutes()
            }
        }

    @Test
    fun `returns failure when getting warehouses fails`() =
        runBlocking {
            // Given
            val error = IllegalStateException(
                "Failed to get warehouses"
            )

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.failure(error)

            // When
            val exception = try {
                useCase("WH-001", "WH-003")
                null
            } catch (e: IllegalStateException) {
                e
            }

            // Then
            assertSame(error, exception)

            coVerify(exactly = 1) {
                warehouseRepository.getAllWarehouses()
            }

            coVerify(exactly = 0) {
                routeRepository.getAllRoutes()
            }
        }

    @Test
    fun `returns failure when getting routes fails`() =
        runBlocking {
            // Given
            val error = IllegalStateException(
                "Failed to get routes"
            )

            stubWarehouses()
            stubRoutesFailure(error)

            // When
            val exception = try {
                useCase("WH-001", "WH-003")
                null
            } catch (e: IllegalStateException) {
                e
            }

            // Then
            assertSame(error, exception)

            coVerify(exactly = 1) {
                warehouseRepository.getAllWarehouses()
            }

            coVerify(exactly = 1) {
                routeRepository.getAllRoutes()
            }
        }

    @Test
    fun `returns empty path when no route exists between warehouses`() =
        runBlocking {
            // Given
            stubWarehouses()

            coEvery {
                routeRepository.getAllRoutes()
            } returns Result.success(emptyList())

            // When
            val result = useCase(
                "WH-001",
                "WH-003"
            )

            // Then
            assertEquals(
                RoutingResult(
                    path = emptyList(),
                    distanceKm = Double.POSITIVE_INFINITY
                ),
                result
            )
        }
}

package org.example.test.domain.usecase

import io.mockk.coEvery
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
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

    @Test
    fun `finds optimal path successfully`() {
        runBlocking {

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.success(warehouses)

            coEvery {
                routeRepository.getAllRoutes()
            } returns Result.success(routes)

            val result = useCase(
                "WH-001",
                "WH-003"
            )

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
        }
    }

    @Test
    fun `throws exception when start warehouse does not exist`() {
        runBlocking {

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.success(warehouses)

            assertFailsWith<WarehouseNotFoundException> {
                useCase(
                    "WH-999",
                    "WH-003"
                )
            }
        }
    }

    @Test
    fun `throws exception when destination warehouse does not exist`() {
        runBlocking {

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.success(warehouses)

            assertFailsWith<WarehouseNotFoundException> {
                useCase(
                    "WH-001",
                    "WH-999"
                )
            }
        }
    }

    @Test
    fun `returns failure when getting warehouses fails`() {
        runBlocking {

            val error = IllegalStateException(
                "Failed to get warehouses"
            )

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.failure(error)

            assertFailsWith<IllegalStateException> {
                useCase(
                    "WH-001",
                    "WH-003"
                )
            }
        }
    }

    @Test
    fun `returns failure when getting routes fails`() {
        runBlocking {

            val error = IllegalStateException(
                "Failed to get routes"
            )

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.success(warehouses)

            coEvery {
                routeRepository.getAllRoutes()
            } returns Result.failure(error)

            assertFailsWith<IllegalStateException> {
                useCase(
                    "WH-001",
                    "WH-003"
                )
            }
        }
    }

    @Test
    fun `returns empty path when no route exists between warehouses`() {
        runBlocking {

            coEvery {
                warehouseRepository.getAllWarehouses()
            } returns Result.success(warehouses)

            coEvery {
                routeRepository.getAllRoutes()
            } returns Result.success(emptyList())

            val result = useCase(
                "WH-001",
                "WH-003"
            )

            assertEquals(
                emptyList(),
                result.path
            )

            assertEquals(
                Double.POSITIVE_INFINITY,
                result.distanceKm
            )
        }
    }
}

package org.example.test.domain.usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.input.FindFewestHopsRouteInput
import org.example.domain.model.result.RoutingResult
import org.example.domain.repository.WarehouseRepository
import org.example.domain.usecase.FindFewestHopsRouteUseCase
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FindFewestHopsRouteUseCaseTest {

    private val warehouseRepository = mockk<WarehouseRepository>()
    private val router = mockk<BreadthFirstSearchRouter>()

    private val useCase = FindFewestHopsRouteUseCase(
        warehouseRepository,
        router
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

    private val input = FindFewestHopsRouteInput(
        startWarehouseId = "WH-001",
        destinationWarehouseId = "WH-003"
    )

    @Test
    fun `finds route with fewest hops successfully`() {
        runBlocking {

            val expectedResult = RoutingResult(
                path = listOf(
                    warehouse1,
                    warehouse2,
                    warehouse3
                ),
                distanceKm = 15.0
            )

            coEvery {
                warehouseRepository.getById("WH-001")
            } returns Result.success(warehouse1)

            coEvery {
                warehouseRepository.getById("WH-003")
            } returns Result.success(warehouse3)

            coEvery {
                router.findPath(
                    start = warehouse1,
                    destination = warehouse3
                )
            } returns expectedResult

            val result = useCase(input)

            assertTrue(result.isSuccess)
            assertEquals(
                expectedResult,
                result.getOrThrow()
            )
        }
    }

    @Test
    fun `returns failure when start warehouse is not found`() {
        runBlocking {

            val error = IllegalStateException(
                "Start warehouse not found"
            )

            coEvery {
                warehouseRepository.getById("WH-001")
            } returns Result.failure(error)

            val result = useCase(input)

            assertTrue(result.isFailure)
            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `returns failure when destination warehouse is not found`() {
        runBlocking {

            val error = IllegalStateException(
                "Destination warehouse not found"
            )

            coEvery {
                warehouseRepository.getById("WH-001")
            } returns Result.success(warehouse1)

            coEvery {
                warehouseRepository.getById("WH-003")
            } returns Result.failure(error)

            val result = useCase(input)

            assertTrue(result.isFailure)
            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }

    @Test
    fun `returns failure when router fails`() {
        runBlocking {

            val error = IllegalStateException(
                "Unable to find route"
            )

            coEvery {
                warehouseRepository.getById("WH-001")
            } returns Result.success(warehouse1)

            coEvery {
                warehouseRepository.getById("WH-003")
            } returns Result.success(warehouse3)

            coEvery {
                router.findPath(
                    start = warehouse1,
                    destination = warehouse3
                )
            } throws error

            val result = useCase(input)

            assertTrue(result.isFailure)
            assertEquals(
                error,
                result.exceptionOrNull()
            )
        }
    }
}

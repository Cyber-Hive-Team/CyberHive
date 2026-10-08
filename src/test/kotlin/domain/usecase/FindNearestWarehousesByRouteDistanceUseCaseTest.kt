package org.example.domain.usecase

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import org.example.domain.algorithm.search.Router
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.InvalidLimitException
import org.example.domain.model.result.RoutingResult
import org.example.domain.repository.WarehouseRepository
import org.junit.jupiter.api.Test

class FindNearestWarehousesByRouteDistanceUseCaseTest {

    private val repository = mockk<WarehouseRepository>()
    private val router = mockk<Router>()
    private val useCase = FindNearestWarehousesByRouteDistanceUseCase(repository, router)

    private val source = warehouse("WH-001")
    private val near = warehouse("WH-002")
    private val far = warehouse("WH-003")


    @Test
    fun `when routes have different distances should sort warehouses by distance`() = runBlocking {
        // Given
        coEvery {
            repository.getAllWarehouses()
        } returns Result.success(
            listOf(source, far, near)
        )

        every {
            router.findPath(source, far)
        } returns RoutingResult(listOf(source, far), 30.0)

        every {
            router.findPath(source, near)
        } returns RoutingResult(listOf(source, near), 10.0)

        // When
        val result = useCase(source, 10).getOrThrow()

        // Then
        assertEquals(
            listOf(near.id, far.id),
            result.map { it.warehouse.id }
        )
    }

    @Test
    fun `when routes have different distances should sort distances in ascending order`() = runBlocking {
        // Given
        coEvery {
            repository.getAllWarehouses()
        } returns Result.success(
            listOf(source, far, near)
        )

        every {
            router.findPath(source, far)
        } returns RoutingResult(listOf(source, far), 30.0)

        every {
            router.findPath(source, near)
        } returns RoutingResult(listOf(source, near), 10.0)

        // When
        val result = useCase(source, 10).getOrThrow()

        // Then
        assertEquals(
            listOf(10.0, 30.0),
            result.map { it.distanceKm }
        )
    }


    @Test
    fun `source warehouse is excluded from route calculation`() = runBlocking {
        // Given
        val warehouseWithSourceId = warehouse("WH-001")
        coEvery {
            repository.getAllWarehouses()
        } returns Result.success(
            listOf(
                warehouseWithSourceId,
                near
            )
        )

        every {
            router.findPath(source, near)
        } returns RoutingResult(
            listOf(source, near),
            10.0
        )

        // When
        useCase(source, 10).getOrThrow()

        // Then
        verify(exactly = 0) { router.findPath(source, warehouseWithSourceId) }
    }

    @Test
    fun `limit restricts number of results`() = runBlocking {
        // Given
        coEvery {
            repository.getAllWarehouses()
        } returns Result.success(
            listOf(source, far, near)
        )

        every {
            router.findPath(source, far)
        } returns RoutingResult(
            listOf(source, far),
            30.0
        )

        every {
            router.findPath(source, near)
        } returns RoutingResult(
            listOf(source, near),
            10.0
        )

        // When
        val result =
            useCase(source, 1)
                .getOrThrow()

        // Then
        assertEquals(
            listOf(near.id),
            result.map { it.warehouse.id }
        )
    }

    @Test
    fun `unreachable warehouses are excluded`() = runBlocking {
        // Given
        coEvery {
            repository.getAllWarehouses()
        } returns Result.success(
            listOf(source, near, far)
        )

        every {
            router.findPath(source, near)
        } returns RoutingResult(
            listOf(source, near),
            10.0
        )

        every {
            router.findPath(source, far)
        } returns RoutingResult(
            emptyList(),
            Double.POSITIVE_INFINITY
        )

        // When
        val result = useCase(source, 10).getOrThrow()

        // Then
        assertEquals(
            listOf(near.id),
            result.map { it.warehouse.id }
        )
    }

    @Test
    fun `empty repository returns empty results`() = runBlocking {
        // Given
        coEvery {
            repository.getAllWarehouses()
        } returns Result.success(emptyList())

        // When
        val result =
            useCase(source, 5)

        // Then
        assertTrue(
            result.getOrThrow().isEmpty()
        )

        verify(exactly = 0) {
            router.findPath(
                any(),
                any()
            )
        }
    }

    @Test
    fun `zero limit throws InvalidLimitException before reading repository`() {
        // Given
        val limit = 0

        // When / Then
        assertFailsWith<InvalidLimitException> {
            runBlocking {
                useCase(source, limit)
            }
        }

        coVerify(exactly = 0) {
            repository.getAllWarehouses()
        }
    }

    @Test
    fun `negative limit throws InvalidLimitException before reading repository`() {
        // Given
        val limit = -1

        // When / Then
        assertFailsWith<InvalidLimitException> {
            runBlocking {
                useCase(source, limit)
            }
        }

        coVerify(exactly = 0) {
            repository.getAllWarehouses()
        }
    }

    @Test
    fun `repository failure is returned without route calculation`() =
        runBlocking {

            // Given
            val error =
                IllegalStateException("Read failed")

            coEvery {
                repository.getAllWarehouses()
            } returns Result.failure(error)

            // When
            val result =
                useCase(source, 5)

            // Then
            assertSame(
                error,
                result.exceptionOrNull()
            )

            verify(exactly = 0) { router.findPath(any(), any()) }
        }

    private fun warehouse(
        id: String
    ) = Warehouse(
        id = id,
        name = "Warehouse $id",
        regionalZone = RegionalZone.NORTH,
        latitude = 31.5,
        longitude = 34.4
    )
}

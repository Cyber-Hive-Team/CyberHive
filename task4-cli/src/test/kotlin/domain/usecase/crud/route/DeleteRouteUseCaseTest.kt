package org.example.test.domain.usecase.crud.route

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository
import org.example.domain.usecase.crud.route.DeleteRouteUseCase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class DeleteRouteUseCaseTest {

    private val repository = mockk<RouteRepository>()

    private val useCase = DeleteRouteUseCase(
        routeRepository = repository
    )

    @Test
    fun `deletes route when route exists`() = runBlocking {

        // Given
        val routeId = "RT-001"
        val route = mockk<Route>()

        coEvery {
            repository.getById(routeId)
        } returns Result.success(route)

        coEvery {
            repository.delete(routeId)
        } returns Result.success(routeId)

        // When
        val result = useCase(routeId)

        // Then
        assertEquals(
            routeId,
            result.getOrThrow()
        )

        coVerify(exactly = 1) {
            repository.getById(routeId)
        }

        coVerify(exactly = 1) {
            repository.delete(routeId)
        }
    }

    @Test
    fun `does not delete route when route does not exist`() = runBlocking {

        // Given
        val routeId = "RT-001"
        val error = IllegalStateException("Route not found")

        coEvery {
            repository.getById(routeId)
        } returns Result.failure(error)

        // When
        val result = useCase(routeId)

        // Then
        assertSame(
            error,
            result.exceptionOrNull()
        )

        coVerify(exactly = 1) {
            repository.getById(routeId)
        }

        coVerify(exactly = 0) {
            repository.delete(any())
        }
    }

    @Test
    fun `returns failure when deleting route fails`() = runBlocking {

        // Given
        val routeId = "RT-001"
        val route = mockk<Route>()
        val error = IllegalStateException("Delete failed")

        coEvery {
            repository.getById(routeId)
        } returns Result.success(route)

        coEvery {
            repository.delete(routeId)
        } returns Result.failure(error)

        // When
        val result = useCase(routeId)

        // Then
        assertSame(
            error,
            result.exceptionOrNull()
        )

        coVerify(exactly = 1) {
            repository.getById(routeId)
        }

        coVerify(exactly = 1) {
            repository.delete(routeId)
        }
    }
}

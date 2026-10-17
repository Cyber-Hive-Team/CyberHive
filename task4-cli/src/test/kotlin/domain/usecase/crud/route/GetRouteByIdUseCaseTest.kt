package org.example.test.domain.usecase.crud.route

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository
import org.example.domain.usecase.crud.route.GetRouteByIdUseCase
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class GetRouteByIdUseCaseTest {

    private val repository = mockk<RouteRepository>()
    private val useCase = GetRouteByIdUseCase(repository)

    @Test
    fun `existing id returns route`() = runBlocking {

        // Given
        val route = mockk<Route>()
        val routeId = "RT-001"

        coEvery {
            repository.getById(routeId)
        } returns Result.success(route)

        // When
        val result = useCase(routeId)

        // Then
        assertSame(
            route,
            result.getOrThrow()
        )

        coVerify(exactly = 1) {
            repository.getById(routeId)
        }
    }

    @Test
    fun `missing route returns failure`() = runBlocking {

        // Given
        val routeId = "RT-999"
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
    }

    @Test
    fun `repository exception becomes failure`() = runBlocking {

        // Given
        val routeId = "RT-001"
        val error = IllegalStateException("Repository failed")

        coEvery {
            repository.getById(routeId)
        } throws error

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
    }
}

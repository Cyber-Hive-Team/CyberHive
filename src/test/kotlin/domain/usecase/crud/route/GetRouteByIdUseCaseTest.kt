package org.example.test.domain.usecase.crud.route

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository
import org.example.domain.usecase.crud.route.GetRouteByIdUseCase
import org.junit.jupiter.api.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue

class GetRouteByIdUseCaseTest {

    private val repository = mockk<RouteRepository>()
    private val useCase = GetRouteByIdUseCase(repository)

    @Test
    fun `existing id returns route`() = runBlocking {

        val route = mockk<Route>()

        coEvery {
            repository.getById("RT-001")
        } returns Result.success(route)

        val result = useCase("RT-001")

        assertSame(route, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.getById("RT-001")
        }
    }

    @Test
    fun `missing route returns failure`() = runBlocking {

        val error = IllegalStateException("Route not found")

        coEvery {
            repository.getById("RT-999")
        } returns Result.failure(error)

        val result = useCase("RT-999")

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun `repository exception becomes failure`() = runBlocking {

        val error = IllegalStateException("Repository failed")

        coEvery {
            repository.getById("RT-001")
        } throws error

        val result = useCase("RT-001")

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }
}

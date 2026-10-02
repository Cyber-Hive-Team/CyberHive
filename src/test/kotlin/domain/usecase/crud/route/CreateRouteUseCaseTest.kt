package org.example.test.domain.usecase.crud.route

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Route
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.usecase.crud.route.CreateRouteUseCase
import org.example.domain.validator.result.*
import org.example.domain.validator.Validator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class CreateRouteUseCaseTest {

    private val repository = mockk<RouteRepository>()
    private val validator = mockk<Validator<Route, UpdateRouteInput>>()

    private val useCase = CreateRouteUseCase(
        routeRepository = repository,
        routeValidator = validator
    )

    @Test
    fun `returns saved route when validation succeeds`() = runBlocking {

        val route = mockk<Route>()

        every {
            validator.validateCreate(route)
        } returns ValidationResult.Success

        coEvery {
            repository.save(route)
        } returns Result.success(route)

        val result = useCase(route)

        assertEquals(route, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.save(route)
        }
    }

    @Test
    fun `does not save route when validation fails`() = runBlocking {

        val route = mockk<Route>()

        every {
            validator.validateCreate(route)
        } returns ValidationResult.Failure(
            listOf(
                FieldViolation(
                    FieldError.InvalidDistance,
                    "Distance must be positive"
                )
            )
        )

        val result = useCase(route)

        assertInstanceOf(
            EntityValidationException::class.java,
            result.exceptionOrNull()
        )

        coVerify(exactly = 0) {
            repository.save(any())
        }
    }

    @Test
    fun `preserves repository failure`() = runBlocking {

        val route = mockk<Route>()
        val error = IllegalStateException("Save failed")

        every {
            validator.validateCreate(route)
        } returns ValidationResult.Success

        coEvery {
            repository.save(route)
        } returns Result.failure(error)

        val result = useCase(route)

        assertSame(
            error,
            result.exceptionOrNull()
        )

        coVerify(exactly = 1) {
            repository.save(route)
        }
    }
}

package org.example.test.domain.usecase.crud.route

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.RegionalZone
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateRouteInput
import org.example.domain.repository.RouteRepository
import org.example.domain.usecase.crud.route.UpdateRouteUseCase
import org.example.domain.validator.FieldError
import org.example.domain.validator.FieldViolation
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.junit.jupiter.api.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class UpdateRouteUseCaseTest {

    private val repository = mockk<RouteRepository>()

    private val validator =
        mockk<Validator<Route, UpdateRouteInput>>()

    private val useCase =
        UpdateRouteUseCase(repository, validator)

    private val originWarehouse = Warehouse(
        "WH-001",
        "Origin",
        RegionalZone.NORTH,
        31.5,
        34.4
    )

    private val destinationWarehouse = Warehouse(
        "WH-002",
        "Destination",
        RegionalZone.SOUTH,
        31.3,
        34.5
    )

    private val route = Route(
        "RT-00001",
        10.0,
        15,
        originWarehouse,
        destinationWarehouse
    )

    private val input = UpdateRouteInput(
        id = "RT-00001",
        distanceKm = 20.0
    )

    @Test
    fun `valid update returns updated route`() = runBlocking {

        val updated = Route(
            "RT-00001",
            20.0,
            15,
            originWarehouse,
            destinationWarehouse
        )

        coEvery {
            repository.getById(input.id)
        } returns Result.success(route)

        every {
            validator.validateUpdate(input)
        } returns ValidationResult.Success

        coEvery {
            repository.update(updated)
        } returns Result.success(updated)

        val result = useCase(route, input)

        assertSame(updated, result.getOrThrow())

        coVerify(exactly = 1) {
            repository.update(updated)
        }
    }

    @Test
    fun `invalid input prevents update`() = runBlocking {

        coEvery {
            repository.getById(input.id)
        } returns Result.success(route)

        every {
            validator.validateUpdate(input)
        } returns ValidationResult.Failure(
            listOf(
                FieldViolation(
                    FieldError.InvalidDistance,
                    "Distance must be positive"
                )
            )
        )

        val result = useCase(route, input)

        assertIs<EntityValidationException>(
            result.exceptionOrNull()
        )

        coVerify(exactly = 0) {
            repository.update(any())
        }
    }

    @Test
    fun `repository update failure is returned`() = runBlocking {

        val error = IllegalStateException("Update failed")

        coEvery {
            repository.getById(input.id)
        } returns Result.success(route)

        every {
            validator.validateUpdate(input)
        } returns ValidationResult.Success

        coEvery {
            repository.update(any())
        } returns Result.failure(error)

        val result = useCase(route, input)

        assertSame(
            error,
            result.exceptionOrNull()
        )
    }
}

package org.example.test.domain.usecase.crud.vehicle

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateVehicleInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.crud.vehicle.CreateVehicleUseCase
import org.example.domain.validator.FieldError
import org.example.domain.validator.FieldViolation
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CreateVehicleUseCaseTest {


    class CreateVehicleUseCaseTest {
        private val factory = TestDataFactory()
        private val repository = mockk<VehicleRepository>()
        private val validator = mockk<Validator<Vehicle, UpdateVehicleInput>>()
        private val useCase = CreateVehicleUseCase(repository, validator)

        @Test
        fun `returns saved vehicle when validation succeeds`() = runBlocking {
            val vehicle = factory.createVehicle()

            every { validator.validateCreate(vehicle) } returns ValidationResult.Success
            coEvery { repository.save(vehicle) } returns Result.success(vehicle)

            val result = useCase(vehicle)

            Assertions.assertEquals(vehicle, result.getOrThrow())
            coVerify(exactly = 1) { repository.save(vehicle) }
        }

        @Test
        fun `does not save when validation fails`() = runBlocking {
            val vehicle = factory.createVehicle(maxCapacityKg = 0.0)

            every { validator.validateCreate(vehicle) } returns ValidationResult.Failure(
                listOf(FieldViolation(FieldError.InvalidCapacity, "invalid capacity"))
            )

            val result = useCase(vehicle)

            Assertions.assertInstanceOf(EntityValidationException::class.java, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.save(any()) }
        }

        @Test
        fun `preserves repository failure`() = runBlocking {
            val vehicle = factory.createVehicle()
            val error = IllegalStateException("Save failed")

            every { validator.validateCreate(vehicle) } returns ValidationResult.Success
            coEvery { repository.save(vehicle) } returns Result.failure(error)

            val result = useCase(vehicle)

            Assertions.assertSame(error, result.exceptionOrNull())
        }
    }

}

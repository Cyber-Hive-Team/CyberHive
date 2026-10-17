package org.example.test.domain.usecase.crud.vehicle

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.model.input.UpdateVehicleInput
import org.example.domain.repository.VehicleRepository
import org.example.domain.usecase.crud.vehicle.UpdateVehicleUseCase
import org.example.domain.validator.result.FieldError
import org.example.domain.validator.result.FieldViolation
import org.example.domain.validator.result.ValidationResult
import org.example.domain.validator.Validator
import org.example.test.TestDataFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
class UpdateVehicleUseCaseTest {


    class UpdateVehicleUseCaseTest {
        private val factory = TestDataFactory()
        private val repository = mockk<VehicleRepository>()
        private val validator = mockk<Validator<Vehicle, UpdateVehicleInput>>()
        private val useCase = UpdateVehicleUseCase(repository, validator)

        @Test
        fun `returns updated vehicle when validation succeeds`() = runBlocking {
            val vehicle = factory.createVehicle(maxCapacityKg = 150.0)
            val input = UpdateVehicleInput(id = vehicle.id, maxCapacityKg = 150.0)

            coEvery { repository.getById(vehicle.id) } returns Result.success(vehicle)
            every { validator.validateUpdate(input) } returns ValidationResult.Success
            coEvery { repository.update(vehicle) } returns Result.success(vehicle)

            val result = useCase(vehicle, input)

            assertEquals(vehicle, result.getOrThrow())
            coVerify(exactly = 1) { repository.update(vehicle) }
        }

        @Test
        fun `does not update when vehicle is missing`() = runBlocking {
            val vehicle = factory.createVehicle()
            val input = UpdateVehicleInput(id = vehicle.id, maxCapacityKg = 150.0)
            val error = VehicleNotFoundException("Vehicle not found")

            coEvery { repository.getById(vehicle.id) } returns Result.failure(error)

            val result = useCase(vehicle, input)

            assertSame(error, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.update(any()) }
        }

        @Test
        fun `does not update when validation fails`() = runBlocking {
            val vehicle = factory.createVehicle()
            val input = UpdateVehicleInput(id = vehicle.id)

            coEvery { repository.getById(vehicle.id) } returns Result.success(vehicle)
            every { validator.validateUpdate(input) } returns ValidationResult.Failure(
                listOf(FieldViolation(FieldError.NoUpdateFields, "no fields"))
            )

            val result = useCase(vehicle, input)

            assertInstanceOf(EntityValidationException::class.java, result.exceptionOrNull())
            coVerify(exactly = 0) { repository.update(any()) }
        }

        @Test
        fun `preserves update failure`() = runBlocking {
            val vehicle = factory.createVehicle()
            val input = UpdateVehicleInput(id = vehicle.id, costPerKm = 3.0)
            val error = IllegalStateException("Update failed")

            coEvery { repository.getById(vehicle.id) } returns Result.success(vehicle)
            every { validator.validateUpdate(input) } returns ValidationResult.Success
            coEvery { repository.update(vehicle) } returns Result.failure(error)

            val result = useCase(vehicle, input)

            assertSame(error, result.exceptionOrNull())
        }
    }
}

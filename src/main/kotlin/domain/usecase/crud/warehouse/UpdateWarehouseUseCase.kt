package org.example.domain.usecase.crud.warehouse

import org.example.domain.model.Warehouse
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.repository.WarehouseRepository
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator

class UpdateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseValidator: Validator<Warehouse, UpdateWarehouseInput>
) {

    suspend operator fun invoke(
        input: UpdateWarehouseInput
    ): Result<Warehouse> {

        return runCatching {

            warehouseRepository.getById(input.id)
                ?: throw WarehouseNotFoundException()


            when (
                val validation =
                    warehouseValidator.validateUpdate(input)
            ) {

                ValidationResult.Success -> {

                    warehouseRepository.update(
                        id = input.id,
                        name = input.name,
                        regionalZone = input.regionalZone,
                        latitude = input.latitude,
                        longitude = input.longitude
                    )
                }


                is ValidationResult.Failure -> {

                    throw EntityValidationException(
                        validation.violations.joinToString {
                            "${it.field}: ${it.message}"
                        }
                    )
                }
            }
        }
    }
}

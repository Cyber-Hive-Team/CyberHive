package org.example.domain.usecase.crud.warehouse

import org.example.domain.model.Warehouse
import org.example.domain.model.exception.EntityValidationException
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.repository.WarehouseRepository
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator

class CreateWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository,
    private val warehouseValidator: Validator<Warehouse, UpdateWarehouseInput>
) {

    suspend operator fun invoke(
        warehouse: Warehouse
    ): Result<Warehouse> {

        return runCatching {

            when (
                val validation =
                    warehouseValidator.validateCreate(warehouse)
            ) {

                ValidationResult.Success -> {
                    warehouseRepository.save(warehouse)
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

package org.example.domain.usecase.crud.warehouse

import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository

class DeleteWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Boolean> {

        return runCatching {

            warehouseRepository.getById(id)
                ?: throw WarehouseNotFoundException()


            warehouseRepository.delete(id)
        }
    }
}

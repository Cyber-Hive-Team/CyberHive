package org.example.domain.usecase.crud.warehouse

import org.example.domain.model.Warehouse
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.repository.WarehouseRepository

class GetWarehouseByIdUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Warehouse> {

        return runCatching {

            warehouseRepository.getById(id)
                ?: throw WarehouseNotFoundException()

        }
    }
}

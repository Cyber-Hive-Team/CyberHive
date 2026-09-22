package org.example.domain.usecase.crud.warehouse

import org.example.domain.repository.WarehouseRepository

class DeleteWarehouseUseCase(
    private val warehouseRepository: WarehouseRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<String> {

        return runCatching {

            warehouseRepository.getById(id)
                .getOrThrow()


            warehouseRepository.delete(id).getOrThrow()
        }
    }
}

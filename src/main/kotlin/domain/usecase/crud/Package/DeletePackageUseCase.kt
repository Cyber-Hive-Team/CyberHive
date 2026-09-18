package org.example.domain.usecase.crud.Package

import org.example.domain.repository.PackageRepository
import org.example.domain.model.exception.PackageNotFoundException

class DeletePackageUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<Boolean> {

        return runCatching {

            packageRepository.getById(id)
                ?: throw PackageNotFoundException()


            packageRepository.delete(id)
        }
    }
}

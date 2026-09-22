package org.example.domain.usecase.crud.packages

import org.example.domain.repository.PackageRepository

class DeletePackageUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        id: String
    ): Result<String> {

        return runCatching {

            packageRepository.getById(id).getOrThrow()

            packageRepository.delete(id).getOrThrow()
        }
    }
}

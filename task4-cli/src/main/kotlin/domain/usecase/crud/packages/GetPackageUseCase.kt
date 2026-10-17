package org.example.domain.usecase.crud.packages

import org.example.domain.model.Package
import org.example.domain.repository.PackageRepository

class GetPackageUseCase(
    private val packageRepository: PackageRepository
) {

    suspend operator fun invoke(
        id: String
    ): Result<Package> {

        return runCatching {

            packageRepository.getById(id)
                .getOrThrow()
        }
    }
}

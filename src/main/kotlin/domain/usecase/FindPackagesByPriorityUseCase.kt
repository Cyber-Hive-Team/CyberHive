package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.result.Result
import org.example.domain.repository.PackageRepository

class FindPackagesByPriorityUseCase(
    private val packageRepository: PackageRepository
) {

    operator fun invoke(priority: Priority): Result<List<Package>> {

        val result = packageRepository.getAllPackages()

        return Result(
            data = result.data.filter { packageItem ->
                packageItem.priority == priority
            },
            errorMessage = result.errorMessage
        )
    }
}

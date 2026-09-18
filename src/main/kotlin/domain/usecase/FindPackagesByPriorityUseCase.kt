package org.example.domain.usecase

import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.repository.PackageRepository

class FindPackagesByPriorityUseCase(
    private val packageRepository: PackageRepository
) {

    operator fun invoke(priority: Priority): Result<List<Package>> {

        return packageRepository
            .getAllPackages()
            .map { packages ->
                packages.filter { packageItem ->
                    packageItem.priority == priority
                }
            }
    }

}

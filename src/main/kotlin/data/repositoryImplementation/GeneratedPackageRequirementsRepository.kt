package org.example.data.repositoryImplementation

import org.example.domain.model.PackageRequirements
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.PackageRequirementsRepository
import kotlin.random.Random

class GeneratedPackageRequirementsRepository(
    private val packageRepository: PackageRepository
) : PackageRequirementsRepository {

    override fun getAllPackageRequirements():
            List<PackageRequirements> {
        return packageRepository
            .getAllPackages()
            .data
            .map { cargoPackage ->
                PackageRequirements(
                    packageId = cargoPackage.id,
                    isFragile = Random.nextBoolean(),
                    requiresColdStorage = Random.nextBoolean(),
                    requiresSpecialHandling = Random.nextBoolean()
                )
            }
    }
}
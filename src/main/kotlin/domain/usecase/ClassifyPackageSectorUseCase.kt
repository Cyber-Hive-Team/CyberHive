
package org.example.domain.usecase

import org.example.domain.algorithm.knn.KnnSectorClassifier
import org.example.domain.model.RegionalZone
import org.example.domain.model.PackageSectorInput
import org.example.domain.repository.PackageRepository

class ClassifyPackageSectorUseCase(
    private val packageRepository: PackageRepository,
    private val classifier: KnnSectorClassifier
) {

    suspend operator fun invoke(
        input: PackageSectorInput
    ): Result<RegionalZone> {
        return runCatching {
            val historicalPackages = packageRepository
                .getAllPackages()
                .getOrThrow()
                .filterNot { it.id == input.packageId }

            classifier.classify(input, historicalPackages)
        }
    }
}

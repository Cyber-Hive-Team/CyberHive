package org.example.domain.usecase.crud.Package

import org.example.domain.model.Package
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.repository.PackageRepository
import org.example.domain.validator.ValidationResult
import org.example.domain.validator.Validator
import org.example.domain.model.exception.EntityValidationException


class CreatePackageUseCase(
    private val packageRepository: PackageRepository,
    private val packageValidator: Validator<Package, UpdatePackageInput>
) {

    suspend operator fun invoke(
        cargoPackage: Package
    ): Result<Package> {

        return runCatching {

            when (
                val validation =
                    packageValidator.validateCreate(cargoPackage)
            ) {

                ValidationResult.Success -> {
                    packageRepository.save(cargoPackage)
                }


                is ValidationResult.Failure -> {

                    throw EntityValidationException(
                        validation.violations.joinToString {
                            "${it.field}: ${it.message}"
                        }
                    )
                }
            }
        }
    }
}

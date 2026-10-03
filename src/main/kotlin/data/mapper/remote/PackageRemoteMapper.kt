package org.example.data.mapper.remote

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.input.UpdatePackageInput


fun PackageResponseDto.toDomainModel(): Package {
    return Package(
        id = id,
        weight = weight.requiredField(
            "Package '$id' has null weight."
        ),
        priority = priority
            ?.let { Priority.valueOf(it) }
            ?: Priority.LOW,
        originWarehouse = originHub.requiredField(
            "Package '$id' has null originHub."
        ).toDomainModel(),
        destinationWarehouse = destinationHub.requiredField(
            "Package '$id' has null destinationHub."
        ).toDomainModel()
    )
}

fun Package.toCreateRequest(): CreatePackageRequestDto {
        return CreatePackageRequestDto(
            id = id,
            weight = weight,
            priority = priority.name,
            originHubId = originWarehouse.id,
            destinationHubId = destinationWarehouse.id
        )

    }

fun UpdatePackageInput.toUpdateRequest(): UpdatePackageRequestDto {

        return UpdatePackageRequestDto(
            weight = weight,
            priority = priority?.name,
            originHubId = originWarehouse.id,
            destinationHubId = destinationWarehouse.id
        )
    }
private fun <T : Any> T?.requiredField(message: String): T {
    return this ?: throw NullRequiredFieldException(message)
}



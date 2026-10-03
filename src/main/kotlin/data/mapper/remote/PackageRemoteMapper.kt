package org.example.data.mapper.remote

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Warehouse
import org.example.domain.model.input.UpdatePackageInput


fun PackageResponseDto.toDomainModel(): Package {

    val originWarehouse = originHub
        ?: throw NullRequiredFieldException("Package '$id' has null originHub.")

    val destinationWarehouse = destinationHub
        ?: throw NullRequiredFieldException("Package '$id' has null destinationHub.")

        return Package(
            id = id,
            weight = weight
                ?: throw NullRequiredFieldException(
                    "Package '${id}' has null weight."
                ),
            priority = priority?.let { Priority.valueOf(it) } ?: Priority.LOW,
            originWarehouse = originWarehouse.toDomainModel(),
            destinationWarehouse = destinationWarehouse.toDomainModel()
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



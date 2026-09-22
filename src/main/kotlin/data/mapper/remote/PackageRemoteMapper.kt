package org.example.data.mapper.remote

import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.domain.model.Package
import org.example.domain.model.Priority
import org.example.domain.model.Warehouse
import org.example.data.exception.NullRequiredFieldException

class PackageRemoteMapper {
    fun mapToDomainModel(
        dto: PackageResponseDto,
        originWarehouse: Warehouse,
        destinationWarehouse: Warehouse
    ): Package {

        return Package(
            id = dto.id,
            weight = dto.weight
                ?: throw NullRequiredFieldException(
                    "Package '${dto.id}' has null weight."
                ),
            priority = dto.priority?.let { Priority.valueOf(it) } ?: Priority.LOW,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )
    }

    fun mapToCreateRequest(
        id: String,
        weight: Double,
        priority: Priority,
        originHubId: String,
        destinationHubId: String
    ): CreatePackageRequestDto {
        return CreatePackageRequestDto(
            id = id,
            weight = weight,
            priority = priority.name,
            originHubId = originHubId,
            destinationHubId = destinationHubId
        )

    }

    fun mapToUpdateRequest(
        weight: Double? = null,
        priority: Priority? = null,
        originHubId: String,
        destinationHubId: String
    ): UpdatePackageRequestDto {
        return UpdatePackageRequestDto(
            weight = weight,
            priority = priority?.name,
            originHubId = originHubId,
            destinationHubId = destinationHubId
        )
    }

}

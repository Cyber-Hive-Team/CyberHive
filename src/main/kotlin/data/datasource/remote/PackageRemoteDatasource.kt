package org.example.data.datasource.remote

import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto

interface PackageRemoteDatasource {

    suspend fun getAll(): List<PackageResponseDto>

    suspend fun getById(
        id: String
    ): PackageResponseDto?

    suspend fun save(
        request: CreatePackageRequestDto
    ): PackageResponseDto

    suspend fun update(
        id: String,
        request: UpdatePackageRequestDto
    ): PackageResponseDto

    suspend fun delete(
        id: String
    ): String
}

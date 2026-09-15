package org.example.data.datasource.remote.supabase

import org.example.data.datasource.remote.PackageRemoteDatasource
import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto

class SupabasePackageRemoteDatasource :
    PackageRemoteDatasource {

    override suspend fun getAll(): List<PackageResponseDto> {
        TODO("Implement Supabase")
    }

    override suspend fun getById(
        id: String
    ): PackageResponseDto? {
        TODO("Implement Supabase")
    }

    override suspend fun save(
        request: CreatePackageRequestDto
    ): PackageResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun update(
        id: String,
        request: UpdatePackageRequestDto
    ): PackageResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        TODO("Implement Supabase")
    }
}

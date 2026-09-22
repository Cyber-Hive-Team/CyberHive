package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.PackageRemoteDatasource
import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto

class SupabasePackageRemoteDatasource(
    private val client: HttpClient,
    private val baseUrl: String
) : PackageRemoteDatasource {

    override suspend fun getAll(): List<PackageResponseDto> {
        return client
            .get("$baseUrl/packages")
            .body()
    }

    override suspend fun getById(
        id: String
    ): PackageResponseDto? {
        return client
            .get("$baseUrl/packages?package_id=eq.$id")
            .body<List<PackageResponseDto>>()
            .firstOrNull()
    }

    override suspend fun save(
        request: CreatePackageRequestDto
    ): PackageResponseDto {
        return client
            .post("$baseUrl/packages") {
                setBody(request)
            }
            .body()
    }

    override suspend fun update(
        id: String,
        request: UpdatePackageRequestDto
    ): PackageResponseDto {
        return client
            .patch("$baseUrl/packages?package_id=eq.$id") {
                setBody(request)
            }
            .body()
    }

    override suspend fun delete(
        id: String
    ): String {
        client.delete("$baseUrl/packages?package_id=eq.$id")
        return id
    }
}

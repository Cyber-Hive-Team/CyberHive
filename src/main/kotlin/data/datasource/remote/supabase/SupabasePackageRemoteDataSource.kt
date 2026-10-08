package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.PackageRemoteDataSource
import org.example.data.remote.dto.request.CreatePackageRequestDto
import org.example.data.remote.dto.request.UpdatePackageRequestDto
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.data.retry.RetryWithBackoff

class SupabasePackageRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
    private val retry: RetryWithBackoff
) : PackageRemoteDataSource {

    private val packageSelect =
        "package_id,weight,origin_hub_id,destination_hub_id,priority," +
                "origin_hub:warehouses!Packages_originHubId_fkey(*)," +
                "destination_hub:warehouses!Packages_destinationHubId_fkey(*)"

    override suspend fun getAll(): List<PackageResponseDto> {
        return retry.executeWithRetry {
            client.get("$baseUrl/packages?select=$packageSelect")
                .body<List<PackageResponseDto>>()
        }.getOrThrow()
    }

    override suspend fun getById(
        id: String
    ): PackageResponseDto? {
        return retry.executeWithRetry {
            client.get(
                "$baseUrl/packages" +
                        "?package_id=eq.$id" +
                        "&select=$packageSelect"
            )
                .body<List<PackageResponseDto>>()
                .firstOrNull()
        }.getOrThrow()
    }

    override suspend fun save(
        request: CreatePackageRequestDto
    ): PackageResponseDto {

        client.post("$baseUrl/packages") {
            setBody(request)
        }

        return getById(request.id)
            ?: error(
                "Package '${request.id}' was saved but could not be retrieved."
            )
    }

    override suspend fun update(
        id: String,
        request: UpdatePackageRequestDto
    ): PackageResponseDto {

        client.patch("$baseUrl/packages?package_id=eq.$id") {
            setBody(request)
        }

        return getById(id)
            ?: error(
                "Package '$id' was updated but could not be retrieved."
            )
    }

    override suspend fun delete(
        id: String
    ): String {
        client.delete("$baseUrl/packages?package_id=eq.$id")
        return id
    }
}

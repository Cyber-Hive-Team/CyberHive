package org.example.data.datasource.remote.supabase

import org.example.data.datasource.remote.WarehouseRemoteDatasource
import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class SupabaseWarehouseRemoteDatasource(
    private val client: HttpClient,
    private val baseUrl: String
) : WarehouseRemoteDatasource {

    override suspend fun getAll(): List<WarehouseResponseDto> {
        return client
            .get("$baseUrl/Warehouses")
            .body()
    }

    override suspend fun getById(
        id: String
    ): WarehouseResponseDto? {
        return client
            .get("$baseUrl/Warehouses?id=eq.$id")
                .body<List<WarehouseResponseDto>>()
            .firstOrNull()
    }

    override suspend fun save(
        request: CreateWarehouseRequestDto
    ): WarehouseResponseDto {
        return client
            .post("$baseUrl/Warehouses") {
                setBody(request)
            }
            .body()
    }

    override suspend fun update(
        id: String,
        request: UpdateWarehouseRequestDto
    ): WarehouseResponseDto {

        return client
            .patch("$baseUrl/Warehouses?id=eq.$id") {
                setBody(request)
            }
            .body()
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        client.delete("$baseUrl/Warehouses?id=eq.$id")
        return true
    }
}

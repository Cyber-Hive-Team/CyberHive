package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.WarehouseRemoteDataSource
import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto
import org.example.data.retry.RetryWithBackoff

class SupabaseWarehouseRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
    private val retry: RetryWithBackoff
) : WarehouseRemoteDataSource {

    override suspend fun getAll(): List<WarehouseResponseDto> {
        return retry.executeWithRetry {
            client.get("$baseUrl/warehouses")
                .body<List<WarehouseResponseDto>>()
        }.getOrThrow()
    }
    override suspend fun getById(
        id: String
    ): WarehouseResponseDto? {
        return retry.executeWithRetry {
            client
                .get("$baseUrl/warehouses?warehouse_id=eq.$id")
                .body<List<WarehouseResponseDto>>()
                .firstOrNull()
        }.getOrThrow()
    }

    override suspend fun save(
        request: CreateWarehouseRequestDto
    ): WarehouseResponseDto {
        return client
            .post("$baseUrl/warehouses") {
                setBody(request)
            }
            .body()
    }

    override suspend fun update(
        id: String,
        request: UpdateWarehouseRequestDto
    ): WarehouseResponseDto {

        return client
            .patch("$baseUrl/warehouses?warehouse_id=eq.$id") {
                setBody(request)
            }
            .body()
    }

    override suspend fun delete(
        id: String
    ): String {
        client.delete("$baseUrl/warehouses?warehouse_id=eq.$id")
        return id
    }
}

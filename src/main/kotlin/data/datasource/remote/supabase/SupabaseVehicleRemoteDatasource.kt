package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.VehicleRemoteDatasource
import org.example.data.remote.dto.request.CreateVehicleRequestDto
import org.example.data.remote.dto.request.UpdateVehicleRequestDto
import org.example.data.remote.dto.response.VehicleResponseDto


class SupabaseVehicleRemoteDatasource(
    private val client: HttpClient,
    private val baseUrl: String
) : VehicleRemoteDatasource {


    override suspend fun getAll(): List<VehicleResponseDto> {

        return client
            .get("$baseUrl/vehicles")
            .body()
    }


    override suspend fun getById(
        id: String
    ): VehicleResponseDto? {

        return client
            .get("$baseUrl/vehicles?vehicle_id=eq.$id")
            .body<List<VehicleResponseDto>>()
            .firstOrNull()
    }


    override suspend fun save(
        request: CreateVehicleRequestDto
    ): VehicleResponseDto {

        return client
            .post("$baseUrl/vehicles") {
                setBody(request)
            }
            .body()
    }


    override suspend fun update(
        id: String,
        request: UpdateVehicleRequestDto
    ): VehicleResponseDto {

        return client
            .patch("$baseUrl/vehicles?vehicle_id=eq.$id") {
                setBody(request)
            }
            .body()
    }


    override suspend fun delete(
        id: String
    ): Boolean {

        client.delete(
            "$baseUrl/vehicles?vehicle_id=eq.$id"
        )

        return true
    }
}

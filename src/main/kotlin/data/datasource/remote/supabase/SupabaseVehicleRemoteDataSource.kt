package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.VehicleRemoteDataSource
import org.example.data.remote.dto.request.CreateVehicleRequestDto
import org.example.data.remote.dto.request.UpdateVehicleRequestDto
import org.example.data.remote.dto.response.VehicleResponseDto


class SupabaseVehicleRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String
) : VehicleRemoteDataSource {

    private val vehicleSelect =
        "vehicle_id,current_hub_id,max_capacity_kg,cost_per_km," +
                "current_hub:warehouses!Vehicles_currentHubId_fkey(*)"

    override suspend fun getAll(): List<VehicleResponseDto> {

        return client
            .get("$baseUrl/vehicles?select=$vehicleSelect")
            .body()
    }


    override suspend fun getById(
        id: String
    ): VehicleResponseDto? {

        return client
            .get("$baseUrl/vehicles" +
                        "?vehicle_id=eq.$id" +
                    "&select=$vehicleSelect"
            ).body<List<VehicleResponseDto>>()
            .firstOrNull()
    }


    override suspend fun save(
        request: CreateVehicleRequestDto
    ): VehicleResponseDto {

        client.post("$baseUrl/vehicles") {
            setBody(request)
        }

        return getById(request.vehicleId)
            ?: throw IllegalStateException(
                "Vehicle '${request.vehicleId}' was saved but could not be retrieved."
            )
    }


    override suspend fun update(
        id: String,
        request: UpdateVehicleRequestDto
    ): VehicleResponseDto {

        client.patch("$baseUrl/vehicles?vehicle_id=eq.$id") {
            setBody(request)
        }

        return getById(id)
            ?: throw IllegalStateException(
                "Vehicle '$id' was updated but could not be retrieved."
            )
    }


    override suspend fun delete(
        id: String
    ): String {

        client.delete(
            "$baseUrl/vehicles?vehicle_id=eq.$id"
        )

        return id
    }
}

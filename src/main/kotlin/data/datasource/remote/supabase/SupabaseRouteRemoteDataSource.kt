package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.RouteRemoteDataSource
import org.example.data.remote.dto.request.CreateRouteRequestDto
import org.example.data.remote.dto.request.UpdateRouteRequestDto
import org.example.data.remote.dto.response.RouteResponseDto

class SupabaseRouteRemoteDataSource(
    private val client: HttpClient,
    private val baseUrl: String
) : RouteRemoteDataSource {

    private val routeSelect =
        "route_id,origin_hub_id,destination_hub_id,distance_km,typical_delay_min," +
                "origin_hub:warehouses!Routes_originHubId_fkey(*)," +
                "destination_hub:warehouses!Routes_destinationHubId_fkey(*)"

    override suspend fun getAll(): List<RouteResponseDto> {
        return client
            .get("$baseUrl/routes?select=$routeSelect")
            .body()
    }

    override suspend fun getById(
        id: String
    ): RouteResponseDto? {
        return client
            .get(
                "$baseUrl/routes" +
                        "?route_id=eq.$id" +
                        "&select=$routeSelect"
            )
            .body<List<RouteResponseDto>>()
            .firstOrNull()
    }

    override suspend fun save(
        request: CreateRouteRequestDto
    ): RouteResponseDto {

        client.post("$baseUrl/routes") {
            setBody(request)
        }

        return getById(request.routeId)
            ?: error(
                "Route '${request.routeId}' was saved but could not be retrieved."
            )
    }

    override suspend fun update(
        id: String,
        request: UpdateRouteRequestDto
    ): RouteResponseDto {

        client.patch("$baseUrl/routes?route_id=eq.$id") {
            setBody(request)
        }

        return getById(id)
            ?: error(
                "Route '$id' was updated but could not be retrieved."
            )
    }

    override suspend fun delete(
        id: String
    ): String {
        client.delete("$baseUrl/routes?route_id=eq.$id")
        return id
    }
}

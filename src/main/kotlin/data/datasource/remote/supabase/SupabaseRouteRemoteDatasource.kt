package org.example.data.datasource.remote.supabase

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import org.example.data.datasource.remote.RouteRemoteDatasource
import org.example.data.remote.dto.request.CreateRouteRequestDto
import org.example.data.remote.dto.request.UpdateRouteRequestDto
import org.example.data.remote.dto.response.RouteResponseDto


class SupabaseRouteRemoteDatasource(
    private val client: HttpClient,
    private val baseUrl: String
) : RouteRemoteDatasource {


    override suspend fun getAll(): List<RouteResponseDto> {

        return client
            .get("$baseUrl/routes")
            .body()
    }


    override suspend fun getById(
        id: String
    ): RouteResponseDto? {

        return client
            .get("$baseUrl/routes?route_id=eq.$id")
            .body<List<RouteResponseDto>>()
            .firstOrNull()
    }


    override suspend fun save(
        request: CreateRouteRequestDto
    ): RouteResponseDto {

        return client
            .post("$baseUrl/routes") {
                setBody(request)
            }
            .body()
    }


    override suspend fun update(
        id: String,
        request: UpdateRouteRequestDto
    ): RouteResponseDto {

        return client
            .patch("$baseUrl/routes?route_id=eq.$id") {
                setBody(request)
            }
            .body()
    }


    override suspend fun delete(
        id: String
    ): Boolean {

        client.delete(
            "$baseUrl/routes?route_id=eq.$id"
        )

        return true
    }
}

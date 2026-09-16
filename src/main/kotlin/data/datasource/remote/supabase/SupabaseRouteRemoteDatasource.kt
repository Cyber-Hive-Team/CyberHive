package org.example.data.datasource.remote.supabase

import data.remote.dto.request.CreateRouteRequestDto
import data.remote.dto.request.UpdateRouteRequestDto
import data.remote.dto.response.RouteResponseDto
import org.example.data.datasource.remote.RouteRemoteDatasource


class SupabaseRouteRemoteDatasource :
    RouteRemoteDatasource {

    override suspend fun getAll(): List<RouteResponseDto> {
        TODO("Implement Supabase")
    }

    override suspend fun getById(
        id: String
    ): RouteResponseDto? {
        TODO("Implement Supabase")
    }

    override suspend fun save(
        request: CreateRouteRequestDto
    ): RouteResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun update(
        id: String,
        request: UpdateRouteRequestDto
    ): RouteResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        TODO("Implement Supabase")
    }
}

package org.example.data.datasource.remote.supabase

import org.example.data.datasource.remote.RouteRemoteDatasource
import data.remote.dto.RouteDto

class SupabaseRouteRemoteDatasource :
    RouteRemoteDatasource {

    override suspend fun getAll(): List<RouteDto> {
        TODO("Implement Supabase")
    }

    override suspend fun getById(
        id: String
    ): RouteDto? {
        TODO("Implement Supabase")
    }

    override suspend fun save(
        route: RouteDto
    ): RouteDto {
        TODO("Implement Supabase")
    }

    override suspend fun update(
        id: String,
        route: RouteDto
    ): RouteDto {
        TODO("Implement Supabase")
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        TODO("Implement Supabase")
    }
}

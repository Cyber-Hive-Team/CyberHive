package org.example.data.datasource.remote

import data.remote.dto.response.RouteResponseDto

interface RouteRemoteDatasource {

    suspend fun getAll(): List<RouteResponseDto>

    suspend fun getById(
        id: String
    ): RouteResponseDto?

    suspend fun save(
        route: RouteResponseDto
    ): RouteResponseDto

    suspend fun update(
        id: String,
        route: RouteResponseDto
    ): RouteResponseDto

    suspend fun delete(
        id: String
    ): Boolean
}

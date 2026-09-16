package org.example.data.datasource.remote

import data.remote.dto.request.CreateRouteRequestDto
import data.remote.dto.request.UpdateRouteRequestDto
import data.remote.dto.response.RouteResponseDto

interface RouteRemoteDatasource {

    suspend fun getAll(): List<RouteResponseDto>

    suspend fun getById(
        id: String
    ): RouteResponseDto?

    suspend fun save(
        request: CreateRouteRequestDto
    ): RouteResponseDto

    suspend fun update(
        id: String,
        request: UpdateRouteRequestDto
    ): RouteResponseDto

    suspend fun delete(
        id: String
    ): Boolean
}

package org.example.data.datasource.remote

import data.remote.dto.RouteDto

interface RouteRemoteDatasource {

    suspend fun getAll(): List<RouteDto>

    suspend fun getById(
        id: String
    ): RouteDto?

    suspend fun save(
        route: RouteDto
    ): RouteDto

    suspend fun update(
        id: String,
        route: RouteDto
    ): RouteDto

    suspend fun delete(
        id: String
    ): Boolean
}

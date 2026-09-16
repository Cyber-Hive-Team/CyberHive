package org.example.data.datasource.remote

import data.remote.dto.response.VehicleResponseDto

interface VehicleRemoteDatasource {

    suspend fun getAll(): List<VehicleResponseDto>

    suspend fun getById(
        id: String
    ): VehicleResponseDto?

    suspend fun save(
        vehicle: VehicleResponseDto
    ): VehicleResponseDto

    suspend fun update(
        id: String,
        vehicle: VehicleResponseDto
    ): VehicleResponseDto

    suspend fun delete(
        id: String
    ): Boolean
}

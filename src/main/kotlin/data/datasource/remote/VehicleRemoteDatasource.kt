package org.example.data.datasource.remote

import data.remote.dto.request.CreateVehicleRequestDto
import data.remote.dto.request.UpdateVehicleRequestDto
import data.remote.dto.response.VehicleResponseDto

interface VehicleRemoteDatasource {

    suspend fun getAll(): List<VehicleResponseDto>

    suspend fun getById(
        id: String
    ): VehicleResponseDto?

    suspend fun save(
        request: CreateVehicleRequestDto
    ): VehicleResponseDto

    suspend fun update(
        id: String,
        request: UpdateVehicleRequestDto
    ): VehicleResponseDto

    suspend fun delete(
        id: String
    ): Boolean
}

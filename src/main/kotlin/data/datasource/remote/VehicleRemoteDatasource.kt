package org.example.data.datasource.remote

import org.example.data.remote.dto.request.CreateVehicleRequestDto
import org.example.data.remote.dto.request.UpdateVehicleRequestDto
import org.example.data.remote.dto.response.VehicleResponseDto

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
    ): String
}

package org.example.data.datasource.remote.supabase

import data.remote.dto.request.CreateVehicleRequestDto
import data.remote.dto.request.UpdateVehicleRequestDto
import data.remote.dto.response.VehicleResponseDto
import org.example.data.datasource.remote.VehicleRemoteDatasource


class SupabaseVehicleRemoteDatasource :
    VehicleRemoteDatasource {

    override suspend fun getAll(): List<VehicleResponseDto> {
        TODO("Implement Supabase")
    }

    override suspend fun getById(
        id: String
    ): VehicleResponseDto? {
        TODO("Implement Supabase")
    }

    override suspend fun save(
        request: CreateVehicleRequestDto
    ): VehicleResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun update(
        id: String,
        request: UpdateVehicleRequestDto
    ): VehicleResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        TODO("Implement Supabase")
    }
}

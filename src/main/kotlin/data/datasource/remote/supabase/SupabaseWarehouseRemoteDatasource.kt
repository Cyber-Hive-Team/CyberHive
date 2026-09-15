package org.example.data.datasource.remote.supabase

import org.example.data.datasource.remote.WarehouseRemoteDatasource
import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto

class SupabaseWarehouseRemoteDatasource :
    WarehouseRemoteDatasource {

    override suspend fun getAll(): List<WarehouseResponseDto> {
        TODO("Implement Supabase")
    }

    override suspend fun getById(
        id: String
    ): WarehouseResponseDto? {
        TODO("Implement Supabase")
    }

    override suspend fun save(
        request: CreateWarehouseRequestDto
    ): WarehouseResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun update(
        id: String,
        request: UpdateWarehouseRequestDto
    ): WarehouseResponseDto {
        TODO("Implement Supabase")
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        TODO("Implement Supabase")
    }
}

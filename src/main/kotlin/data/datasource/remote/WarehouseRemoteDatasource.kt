package org.example.data.datasource.remote

import org.example.data.remote.dto.request.CreateWarehouseRequestDto
import org.example.data.remote.dto.request.UpdateWarehouseRequestDto
import org.example.data.remote.dto.response.WarehouseResponseDto

interface WarehouseRemoteDatasource {

    suspend fun getAll(): List<WarehouseResponseDto>

    suspend fun getById(
        id: String
    ): WarehouseResponseDto?

    suspend fun save(
        request: CreateWarehouseRequestDto
    ): WarehouseResponseDto

    suspend fun update(
        id: String,
        request: UpdateWarehouseRequestDto
    ): WarehouseResponseDto

    suspend fun delete(
        id: String
    ): String
}

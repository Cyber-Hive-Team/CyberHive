package org.example.data.datasource.remote

import data.remote.dto.VehicleDto

interface VehicleRemoteDatasource {

    suspend fun getAll(): List<VehicleDto>

    suspend fun getById(
        id: String
    ): VehicleDto?

    suspend fun save(
        vehicle: VehicleDto
    ): VehicleDto

    suspend fun update(
        id: String,
        vehicle: VehicleDto
    ): VehicleDto

    suspend fun delete(
        id: String
    ): Boolean
}

package org.example.data.datasource.remote.supabase

import org.example.data.datasource.remote.VehicleRemoteDatasource
import org.example.data.remote.dto.VehicleDto

class SupabaseVehicleRemoteDatasource :
    VehicleRemoteDatasource {

    override suspend fun getAll(): List<VehicleDto> {
        TODO("Implement Supabase")
    }

    override suspend fun getById(
        id: String
    ): VehicleDto? {
        TODO("Implement Supabase")
    }

    override suspend fun save(
        vehicle: VehicleDto
    ): VehicleDto {
        TODO("Implement Supabase")
    }

    override suspend fun update(
        id: String,
        vehicle: VehicleDto
    ): VehicleDto {
        TODO("Implement Supabase")
    }

    override suspend fun delete(
        id: String
    ): Boolean {
        TODO("Implement Supabase")
    }
}

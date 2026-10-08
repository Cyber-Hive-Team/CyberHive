package org.example.data.repositoryImplementation

import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.remote.VehicleRemoteDataSource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.csv.toDomainModel
import org.example.data.mapper.mapFailureToDomain
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toUpdateRequest
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.domain.model.Vehicle
import org.example.data.datasource.local.model.VehicleLocalData
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository
import org.example.data.mapper.sync.toRaw


class VehicleRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val localDataSource: VehicleDataSource,
) : BaseRepository(), VehicleRepository {

    private val dataExceptionMapper = DataExceptionMapper()

    private val vehicles = mutableListOf<Vehicle>()

    private var isLoaded = false



    override suspend fun getVehicles(): Result<List<Vehicle>> {
        return runCatching {
            if (isLoaded) {
                return@runCatching vehicles.toList()
            }

            val loadedVehicles = loadLocalVehicles()

            vehicles.addAll(loadedVehicles)
            isLoaded = true

            vehicles.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }


    private fun loadLocalVehicles(): List<Vehicle> {
        return localDataSource
            .getVehicles()
            .mapNotNull { result ->
                result.rawData?.let { data ->
                    mapLocalVehicleSafely(data)
                }
            }
    }

    override suspend fun getVehiclesByWarehouseId(
        warehouseId: String
    ): Result<List<Vehicle>> {

        return getVehicles()
            .mapCatching { vehicles ->
                vehicles.filter {
                    it.currentHub.id == warehouseId
                }
            }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun reassignVehicle(
        vehicleId: String,
        warehouseId: String
    ): Result<Boolean> {

        return runCatching {
            getVehicles().getOrThrow()
            val index = vehicles.indexOfFirst { it.id == vehicleId }
            if (index == -1) { return@runCatching false }
            val currentVehicle = vehicles[index]
            val request = currentVehicle.toUpdateRequest(currentHubId = warehouseId)
            val dto = remoteDataSource.update(id = vehicleId, request = request)

            val updatedVehicle = mapRemoteVehicleSafely(dto)
                    ?: throw NullRequiredFieldException("Vehicle '$vehicleId' reassignment mapping failed.")

            vehicles[index] = updatedVehicle
            refreshLocalSnapshot()
            true
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun removeVehicle(
        vehicleId: String
    ): Result<Boolean> {
        return runCatching {
            getVehicles()
                .getOrThrow()

            vehicles.removeIf {
                it.id == vehicleId
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }

    override suspend fun getById(
        vehicleId: String
    ): Result<Vehicle> {
        return getVehicles()
            .mapCatching { vehicles ->
                vehicles.firstOrNull {
                    it.id == vehicleId
                } ?: throw VehicleNotFoundException()
            }
            .mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun save(
        vehicle: Vehicle
    ): Result<Vehicle> {
        return runCatching {
            val request =
                vehicle.toCreateRequest()
            val dto =
                remoteDataSource
                    .save(request)
            val savedVehicle =
                mapRemoteVehicleSafely(dto)
                    ?: throw NullRequiredFieldException("Vehicle '${vehicle.id}' save mapping failed.")
            vehicles.add(savedVehicle)
            refreshLocalSnapshot()
            savedVehicle
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        vehicle: Vehicle
    ): Result<Vehicle> {
        return runCatching {
            val request =
                vehicle.toUpdateRequest()
        val dto =
            remoteDataSource
                .update(
                    id = vehicle.id,
                    request = request
                )
        val updatedVehicle =
            mapRemoteVehicleSafely(dto)
                ?: throw NullRequiredFieldException("Vehicle '${vehicle.id}' update mapping failed.")
        vehicles.removeIf {
            it.id == vehicle.id
        }
        vehicles.add(updatedVehicle)
            refreshLocalSnapshot()
            updatedVehicle
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun delete(
        id: String
    ): Result<String> {
        return runCatching {
            val deletedId = remoteDataSource.delete(id)
            vehicles.removeIf {
                it.id == deletedId
            }
            refreshLocalSnapshot()
            deletedId

        }.mapFailureToDomain(dataExceptionMapper)
    }
    private fun mapRemoteVehicleSafely(
        dto: VehicleResponseDto
    ): Vehicle? {
        return mapSafely(dto.vehicleId) {
            dto.toDomainModel()
        }
    }

    private fun mapLocalVehicleSafely(
        data: VehicleLocalData
    ): Vehicle? {
        return mapSafely(data.vehicleRaw.id) {
            data.toDomainModel()
        }
    }
    private suspend fun refreshLocalSnapshot() {
        val rawVehicles = remoteDataSource
            .getAll()
            .map { dto ->
                dto.toRaw()
            }

        localDataSource.replaceAll(rawVehicles)
    }
}

package org.example.data.repositoryImplementation

import org.example.data.datasource.remote.VehicleRemoteDatasource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.domain.model.Vehicle
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toUpdateRequest


class VehicleRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDatasource,
    private val warehouseMap: Map<String, Warehouse>
) : BaseRepository(), VehicleRepository {

    private val dataExceptionMapper = DataExceptionMapper()

    private val vehicles = mutableListOf<Vehicle>()

    private var isLoaded = false

    override suspend fun getVehicles(): Result<List<Vehicle>> {

        return runCatching {

            if (isLoaded) {
                return@runCatching vehicles.toList()
            }

            val loadedVehicles = remoteDataSource.getAll().mapNotNull { mapVehicleSafely(it) }

            vehicles.addAll(loadedVehicles)
            isLoaded = true


            vehicles.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }


    private fun mapVehicleSafely(
        dto: VehicleResponseDto
    ): Vehicle? {

        return mapSafely(dto.vehicleId) {

            val currentHub =
                warehouseMap[dto.currentHubId]
                    ?: throw NullRequiredFieldException(
                        "Vehicle '${dto.vehicleId}' current hub not found."
                    )

            dto.toDomainModel(
                currentHub = currentHub
            )
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
            getVehicles()
                .getOrThrow()
            val index =
                vehicles.indexOfFirst {
                    it.id == vehicleId
                }


            val targetWarehouse =
                warehouseMap[warehouseId]

            if (
                index == -1 ||
                targetWarehouse == null
            ) {
                return@runCatching false
            }
            val oldVehicle = vehicles[index]
            vehicles[index] =
                Vehicle(
                    id = oldVehicle.id,
                    currentHub = targetWarehouse,
                    maxCapacityKg = oldVehicle.maxCapacityKg,
                    costPerKm = oldVehicle.costPerKm
                )
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

        val cachedVehicle = vehicles.firstOrNull {
            it.id == vehicleId
        }
        if (cachedVehicle != null) {
            return Result.success(cachedVehicle)
        }
        return runCatching {
        val dto =
            remoteDataSource
                .getById(vehicleId)
                ?: throw VehicleNotFoundException()
        val vehicle =
            mapVehicleSafely(dto)
                ?: throw NullRequiredFieldException("Vehicle '$vehicleId' mapping failed.")
            vehicles.add(vehicle)

            vehicle
        }.mapFailureToDomain(dataExceptionMapper)
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
                mapVehicleSafely(dto)
                    ?: throw NullRequiredFieldException("Vehicle '${vehicle.id}' save mapping failed.")
            vehicles.add(savedVehicle)
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
            mapVehicleSafely(dto)
                ?: throw NullRequiredFieldException("Vehicle '${vehicle.id}' update mapping failed.")
        vehicles.removeIf {
            it.id == vehicle.id
        }
        vehicles.add(updatedVehicle)
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
            deletedId

        }.mapFailureToDomain(dataExceptionMapper)
    }
}

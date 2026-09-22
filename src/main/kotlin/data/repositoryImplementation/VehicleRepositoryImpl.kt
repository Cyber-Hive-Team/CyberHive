package org.example.data.repositoryImplementation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.mapFailureToDomain
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.data.repositoryImplementation.dependencies.VehicleRepositoryDependencies
import org.example.domain.model.Vehicle
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository

class VehicleRepositoryImpl(
    private val dependencies: VehicleRepositoryDependencies,
    private val dataExceptionMapper: DataExceptionMapper = DataExceptionMapper()
) : BaseRepository(), VehicleRepository {

    private val vehicles =
        mutableListOf<Vehicle>()

    private var isLoaded = false

    override suspend fun getVehicles(): Result<List<Vehicle>> {

        return runCatching {

            if (isLoaded) {
                return@runCatching vehicles.toList()
            }


            val loadedVehicles =
                dependencies.remoteDataSource
                    .getAll()
                    .mapNotNull {
                        mapVehicleSafely(it)
                    }


            vehicles.addAll(
                loadedVehicles
            )


            isLoaded = true


            vehicles.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }


    private fun mapVehicleSafely(
        dto: VehicleResponseDto
    ): Vehicle? {

        return mapSafely(dto.vehicleId) {

            val currentHub =
                dependencies.warehouseMap[dto.currentHubId]
                    ?: throw NullRequiredFieldException(
                        "Vehicle '${dto.vehicleId}' current hub not found."
                    )

            dependencies.remoteMapper
                .mapToDomain(
                    raw = dto,
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
                dependencies.warehouseMap[warehouseId]

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
            dependencies.remoteDataSource
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
                dependencies.remoteMapper
                    .mapToCreateRequest(vehicle)
            val dto =
                dependencies.remoteDataSource
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
                dependencies.remoteMapper
                    .mapToUpdateRequest(vehicle)
        val dto =
            dependencies.remoteDataSource
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
            val deletedId = dependencies.remoteDataSource.delete(id)
            vehicles.removeIf {
                it.id == deletedId
            }
            deletedId

        }.mapFailureToDomain(dataExceptionMapper)
    }
}

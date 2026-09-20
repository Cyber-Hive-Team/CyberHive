package org.example.data.repositoryImplementation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.data.repositoryImplementation.dependencies.VehicleRepositoryDependencies
import org.example.domain.model.Vehicle
import org.example.domain.repository.VehicleRepository


class VehicleRepositoryImpl(
    private val dependencies: VehicleRepositoryDependencies
) : VehicleRepository {

    private val vehicles =
        mutableListOf<Vehicle>()
    private val warnings =
        mutableListOf<String>()
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
        }
    }


    private fun mapVehicleSafely(
        dto: VehicleResponseDto
    ): Vehicle? {

        return runCatching {

            val currentHub =
                dependencies.warehouseMap[dto.currentHubId]
                    ?: throw NullRequiredFieldException(
                        "Vehicle '${dto.vehicleId}' current hub not found."
                    )


            dependencies.remoteValidator
                .validate(dto)


            dependencies.remoteMapper
                .mapToDomain(
                    raw = dto,
                    currentHub = currentHub
                )

        }.getOrElse { exception ->

            if (exception is NullRequiredFieldException) {

                warnings.add(
                    "Vehicle '${dto.vehicleId}': ${exception.message}"
                )

                null

            } else {

                throw exception
            }
        }
    }


    override suspend fun getVehiclesByWarehouseId(
        warehouseId: String
    ): Result<List<Vehicle>> {

        return getVehicles()
            .map { vehicles ->
                vehicles.filter {
                    it.currentHub.id == warehouseId
                }
            }
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
        }
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
        }
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
                ?: error("Vehicle with id '$vehicleId' was not found.")

        val vehicle =
            mapVehicleSafely(dto) ?: error("Vehicle mapping failed.")
            vehicles.add(vehicle)

            vehicle
        }
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
                    ?: vehicle
            vehicles.add(savedVehicle)
            savedVehicle
        }
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
                ?: vehicle
        vehicles.removeIf {
            it.id == vehicle.id
        }
        vehicles.add(updatedVehicle)
            updatedVehicle
        }
    }


    override suspend fun delete(
        id: String
    ): Result<Boolean> {
        return runCatching {
        val deleted =
            dependencies.remoteDataSource
                .delete(id)
        if (deleted) {
            vehicles.removeIf {
                it.id == id
            }
        }
            deleted
    }
    }
}

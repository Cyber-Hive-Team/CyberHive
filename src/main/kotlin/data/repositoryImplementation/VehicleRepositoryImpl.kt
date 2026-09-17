package org.example.data.repositoryImplementation

import org.example.data.repositoryImplementation.dependencies.VehicleRepositoryDependencies
import org.example.domain.model.Vehicle
import org.example.domain.model.result.Result
import org.example.domain.repository.VehicleRepository

class VehicleRepositoryImpl(
    private val dependencies: VehicleRepositoryDependencies
) : VehicleRepository {

    private val vehicles = mutableListOf<Vehicle>()
    private var isLoaded = false


    @Suppress("TooGenericExceptionCaught", "LongMethod", "ReturnCount")
    override fun getVehicles(): Result<List<Vehicle>> {

        return try {

            if (isLoaded) {
                return Result(
                    data = vehicles.toList(),
                    errorMessage = null
                )
            }


            val rawResults =
                dependencies.localDataSource.getVehicles()


            val warnings =
                rawResults
                    .mapNotNull { it.errorMessage }
                    .toMutableList()


            val rawVehicles =
                rawResults
                    .mapNotNull { it.rawData }


            rawVehicles.forEach { raw ->

                val currentHub =
                    dependencies.warehouseMap[raw.currentHubId]


                val validationWarnings =
                    dependencies.validator.validate(
                        raw,
                        currentHub
                    )


                if (validationWarnings.isEmpty()) {

                    vehicles.add(
                        dependencies.localMapper.map(
                            raw,
                            currentHub!!
                        )
                    )

                } else {

                    warnings.addAll(validationWarnings)

                }
            }


            isLoaded = true


            Result(
                data = vehicles.toList(),
                errorMessage =
                    warnings
                        .takeIf { it.isNotEmpty() }
                        ?.joinToString("; ")
            )


        } catch (e: Exception) {

            Result(
                data = emptyList(),
                errorMessage =
                    "Failed to load vehicles: ${e.message}"
            )
        }
    }


    override fun getVehicleById(
        vehicleId: String
    ): Vehicle? {

        return getVehicles()
            .data
            .firstOrNull {
                it.id == vehicleId
            }
    }


    override fun getVehiclesByWarehouseId(
        warehouseId: String
    ): Result<List<Vehicle>> {

        val result = getVehicles()

        return Result(
            data = result.data.filter { vehicle ->
                vehicle.currentHub.id == warehouseId
            },
            errorMessage = result.errorMessage
        )
    }


    override fun reassignVehicle(
        vehicleId: String,
        warehouseId: String
    ): Boolean {
        getVehicles()
        val index =
            vehicles.indexOfFirst { vehicle ->
                vehicle.id == vehicleId
            }
        val targetWarehouse =
            dependencies.warehouseMap[warehouseId]
        if (index == -1 || targetWarehouse == null) {
            return false
        }
        vehicles[index] =
            Vehicle(
                id = vehicles[index].id,
                currentHub = targetWarehouse,
                maxCapacityKg = vehicles[index].maxCapacityKg,
                costPerKm = vehicles[index].costPerKm
            )
        return true

    }


    override fun removeVehicle(
        vehicleId: String
    ): Boolean {

        getVehicles()

        return vehicles.removeIf {
            it.id == vehicleId
        }
    }



    override suspend fun getRemoteById(
        vehicleId: String
    ): Vehicle? {


        val responseDto =
            dependencies.remoteDataSource
                .getById(vehicleId)
                ?: return null


        val currentHub =
            responseDto.currentHubId
                ?.let {
                    dependencies
                        .warehouseRepository
                        .getWarehouseById(it)
                }

        return currentHub?.let {
            dependencies.remoteMapper
            .mapToDomain(
                raw = responseDto,
                currentHub = currentHub
            )
        }
    }



    override suspend fun save(
        vehicle: Vehicle
    ): Vehicle {


        val request =
            dependencies.remoteMapper
                .mapToCreateRequest(vehicle)



        val responseDto =
            dependencies.remoteDataSource
                .save(request)



        val currentHub =
            responseDto.currentHubId
                ?.let {
                    dependencies
                        .warehouseRepository
                        .getWarehouseById(it)
                }
                ?: return vehicle



        return dependencies.remoteMapper
            .mapToDomain(
                raw = responseDto,
                currentHub = currentHub
            )
    }


    override suspend fun update(
        vehicle: Vehicle
    ): Vehicle {


        val request =
            dependencies.remoteMapper
                .mapToUpdateRequest(vehicle)



        val responseDto =
            dependencies.remoteDataSource
                .update(
                    id = vehicle.id,
                    request = request
                )


        val currentHub =
            responseDto.currentHubId
                ?.let {
                    dependencies
                        .warehouseRepository
                        .getWarehouseById(it)
                }
                ?: return vehicle



        return dependencies.remoteMapper
            .mapToDomain(
                raw = responseDto,
                currentHub = currentHub
            )
    }


    override suspend fun delete(
        id: String
    ): Boolean {

        return dependencies.remoteDataSource
            .delete(id)
    }
}

package org.example.data.repositoryImplementation


import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.example.data.datasource.VehicleDataSource
import org.example.data.datasource.local.model.VehicleLocalData
import org.example.data.datasource.remote.VehicleRemoteDataSource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.csv.toDomainModel
import org.example.data.mapper.mapFailureToDomain
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toUpdateRequest
import org.example.data.mapper.sync.toRaw
import org.example.data.remote.dto.response.VehicleResponseDto
import org.example.data.telemetry.TelemetryStreamingService
import org.example.data.util.suspendResult
import org.example.domain.model.Vehicle
import org.example.domain.model.VehicleTelemetry
import org.example.domain.model.exception.VehicleNotFoundException
import org.example.domain.repository.VehicleRepository


class VehicleRepositoryImpl(
    private val remoteDataSource: VehicleRemoteDataSource,
    private val localDataSource: VehicleDataSource,
    private val telemetryStreamingService: TelemetryStreamingService
) : BaseRepository(), VehicleRepository {
    private val cacheMutex = Mutex()
    private val dataExceptionMapper = DataExceptionMapper()

    private val vehicles = mutableListOf<Vehicle>()

    private var isLoaded = false



    override suspend fun getVehicles(): Result<List<Vehicle>> {
        return suspendResult {
            cacheMutex.withLock {
                loadVehiclesIfNeeded()
                vehicles.toList()
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }

    private suspend fun loadVehiclesIfNeeded() {
        if (isLoaded) return

        val loadedVehicles = loadLocalVehicles()

        vehicles.addAll(loadedVehicles)
        isLoaded = true
    }

    private suspend fun loadLocalVehicles(): List<Vehicle> {
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
        return suspendResult {
            cacheMutex.withLock {
                loadVehiclesIfNeeded()

                val index = vehicles.indexOfFirst { it.id == vehicleId }
                if (index == -1) return@withLock false

                val currentVehicle = vehicles[index]
                val request = currentVehicle.toUpdateRequest(
                    currentHubId = warehouseId
                )
                val dto = remoteDataSource.update(vehicleId, request)

                val updatedVehicle = mapRemoteVehicleSafely(dto)
                    ?: throw NullRequiredFieldException(
                        "Vehicle '$vehicleId' reassignment mapping failed."
                    )

                vehicles[index] = updatedVehicle
                refreshLocalSnapshot()
                true
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun removeVehicle(
        vehicleId: String
    ): Result<Boolean> {
        return suspendResult {
            cacheMutex.withLock {
                loadVehiclesIfNeeded()
                vehicles.removeIf { it.id == vehicleId }
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
        return suspendResult  {
            cacheMutex.withLock {
                loadVehiclesIfNeeded()
                val request = vehicle.toCreateRequest()
                val dto = remoteDataSource.save(request)
                val savedVehicle = mapRemoteVehicleSafely(dto)
                    ?: throw NullRequiredFieldException("Vehicle '${vehicle.id}' save mapping failed.")
                vehicles.add(savedVehicle)
                refreshLocalSnapshot()
                savedVehicle
            }}.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        vehicle: Vehicle
    ): Result<Vehicle> {
        return suspendResult  {
            cacheMutex.withLock {
                loadVehiclesIfNeeded()
                val request = vehicle.toUpdateRequest()
                val dto = remoteDataSource.update(id = vehicle.id, request = request)
                val updatedVehicle =
                    mapRemoteVehicleSafely(dto)
                        ?: throw NullRequiredFieldException("Vehicle '${vehicle.id}' update mapping failed.")
                vehicles.removeIf {
                    it.id == vehicle.id
                }
                vehicles.add(updatedVehicle)
                refreshLocalSnapshot()
                updatedVehicle
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun delete(
        id: String
    ): Result<String> {
        return suspendResult  {
            cacheMutex.withLock {
                loadVehiclesIfNeeded()
                val deletedId = remoteDataSource.delete(id)
                vehicles.removeIf {
                    it.id == deletedId
                }
                refreshLocalSnapshot()
                deletedId

            }
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

    override fun streamTelemetry(
        vehicleIds: List<String>
    ): Flow<VehicleTelemetry> = flow {

        val requestedIds = vehicleIds.toSet()

        val vehicles = getVehicles()
            .getOrThrow()
            .filter { it.id in requestedIds }

        val foundIds = vehicles.map { it.id }.toSet()

        require(foundIds == requestedIds) {
            "Some requested vehicles were not found"
        }

        emitAll(
            telemetryStreamingService.stream(vehicles)
        )
    }
}

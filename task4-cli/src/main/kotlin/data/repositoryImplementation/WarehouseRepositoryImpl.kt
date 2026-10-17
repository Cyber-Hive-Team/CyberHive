package org.example.data.repositoryImplementation

import kotlin.random.Random
import org.example.data.datasource.WarehouseDataSource
import org.example.data.datasource.local.csv.CsvWarehouseStatusDataSource
import org.example.data.datasource.remote.WarehouseRemoteDataSource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.csv.toDomainModel
import org.example.data.mapper.mapFailureToDomain
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toUpdateRequest
import org.example.domain.model.Package
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.WarehouseStatus
import org.example.domain.model.exception.WarehouseNotFoundException
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.repository.WarehouseRepository
import org.example.data.mapper.sync.toRaw
import org.example.data.util.suspendResult
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WarehouseRepositoryImpl(
    private val remoteDataSource: WarehouseRemoteDataSource,
    private val localDataSource: WarehouseDataSource,
    private val statusDataSource: CsvWarehouseStatusDataSource
) : BaseRepository(), WarehouseRepository {

    private val cacheMutex = Mutex()
    private val statusMutex = Mutex()
    private val dataExceptionMapper = DataExceptionMapper()

    private val warehouses =
        mutableListOf<Warehouse>()


    private var isLoaded = false


    override suspend fun getAllWarehouses(): Result<List<Warehouse>> {
        return suspendResult {
            cacheMutex.withLock {
                loadWarehousesIfNeeded()
                warehouses.toList()
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }

    private suspend fun loadWarehousesIfNeeded() {
        if (isLoaded) return

        val loadedWarehouses = localDataSource.getWarehouses().
        mapNotNull { result -> result.rawData?.let { raw -> mapSafely(raw.id) { raw.toDomainModel() } }
            }
        warehouses.addAll(loadedWarehouses)
        isLoaded = true
    }

    override suspend fun addPackageToCargoQueue(
        warehouseId: String,
        cargoPackage: Package
    ): Result<Boolean> {
        return suspendResult {
            cacheMutex.withLock {
                val warehouse = findWarehouseInCache(warehouseId)
                warehouse.addPackages(listOf(cargoPackage))
                true
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun sortCargoQueue(
        warehouseId: String
    ): Result<Boolean> {
        return suspendResult {
            cacheMutex.withLock {
                findWarehouseInCache(warehouseId).sortCargoQueue()
                true
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun isPackageInCargoQueue(
        warehouseId: String,
        packageId: String
    ): Result<Boolean> {
        return suspendResult {
            cacheMutex.withLock {
                findWarehouseInCache(warehouseId)
                    .getCargoQueue()
                    .any { it.id == packageId }
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun getAllWarehouseServices():
            Result<List<WarehouseServices>> {
        return getAllWarehouses()
            .mapCatching { warehouses ->
                warehouses.map { warehouse ->
                    WarehouseServices(
                        warehouseId = warehouse.id,
                        supportsFragileHandling =
                            Random.nextBoolean(),
                        supportsColdStorage =
                            Random.nextBoolean(),
                        supportsSpecialHandling =
                            Random.nextBoolean()
                    )
                }
            }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun getById(
        id: String
    ): Result<Warehouse> {
        return getAllWarehouses()
            .mapCatching { warehouses ->
                warehouses.firstOrNull {
                    it.id == id
                } ?: throw WarehouseNotFoundException()
            }
            .mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun save(
        warehouse: Warehouse
    ): Result<Warehouse> {
        return suspendResult  {
            cacheMutex.withLock {
                loadWarehousesIfNeeded()
            val requestDto = warehouse.toCreateRequest()
            val responseDto =
                remoteDataSource
                    .save(requestDto)
                val savedWarehouse = mapSafely(responseDto.id) {
                    responseDto.toDomainModel()
                } ?: throw NullRequiredFieldException(
                    "Warehouse '${responseDto.id}' mapping failed."
                )
            warehouses.add(savedWarehouse)
            refreshLocalSnapshot()
            savedWarehouse
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }

    override suspend fun update(
        input: UpdateWarehouseInput
    ): Result<Warehouse> {
        return suspendResult  {
            cacheMutex.withLock {
                loadWarehousesIfNeeded()
                val requestDto = input.toUpdateRequest()
                val responseDto =
                    remoteDataSource
                        .update(id = input.id, request = requestDto)
                val updatedWarehouse = mapSafely(responseDto.id) {
                    responseDto.toDomainModel()
                } ?: throw NullRequiredFieldException(
                    "Warehouse '${input.id}' update failed."
                )
                warehouses.removeIf {
                    it.id == input.id
                }
                warehouses.add(updatedWarehouse)
                refreshLocalSnapshot()
                updatedWarehouse
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }
    override suspend fun delete(
        id: String
    ): Result<String> {
        return suspendResult  {
            cacheMutex.withLock {
                loadWarehousesIfNeeded()
                val deletedId = remoteDataSource.delete(id)
                warehouses.removeIf {
                    it.id == deletedId
                }
                refreshLocalSnapshot()
                deletedId
            } }.mapFailureToDomain(dataExceptionMapper)

    }

    override suspend fun getStatus(
        warehouseId: String
    ): Result<WarehouseStatus> {
        return suspendResult {
            statusMutex.withLock {
                withContext(Dispatchers.IO) {
                    statusDataSource.getStatus(warehouseId)
                }
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }

    override suspend fun updateStatus(
        warehouseId: String,
        status: WarehouseStatus
    ): Result<Boolean> {
        return suspendResult {
            statusMutex.withLock {
                withContext(Dispatchers.IO) {
                    statusDataSource.updateStatus(warehouseId, status)
                }
            }
        }.mapFailureToDomain(dataExceptionMapper)
    }

    private suspend fun refreshLocalSnapshot() {
        val rawWarehouses = remoteDataSource
            .getAll()
            .map { dto ->
                dto.toRaw()
            }

        localDataSource.replaceAll(rawWarehouses)
    }

    private suspend fun findWarehouseInCache(
        warehouseId: String
    ): Warehouse {
        loadWarehousesIfNeeded()

        return warehouses.firstOrNull { it.id == warehouseId }
            ?: throw WarehouseNotFoundException()
    }
}

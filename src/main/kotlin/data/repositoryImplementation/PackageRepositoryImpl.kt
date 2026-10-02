package org.example.data.repositoryImplementation

import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.example.data.dataholder.PackageRaw
import org.example.data.datasource.PackageDataSource
import org.example.data.datasource.remote.PackageRemoteDatasource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.csv.toDomainModel
import org.example.data.mapper.mapFailureToDomain
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toUpdateRequest
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.domain.model.Package
import org.example.domain.model.PackageRequirements
import org.example.domain.model.PackageWarehouseStay
import org.example.domain.model.Warehouse
import org.example.domain.model.exception.PackageNotFoundException
import org.example.domain.model.input.PackageDeliveryTime
import org.example.domain.model.input.UpdatePackageInput
import org.example.domain.repository.PackageRepository

private const val MIN_WAITING_HOURS = 1L
private const val MAX_WAITING_HOURS = 73L

private const val MIN_EXPECTED_HOURS = 2L
private const val MAX_EXPECTED_HOURS = 10L

private const val MIN_ARRIVAL_OFFSET_MINUTES = -60L
private const val MAX_ARRIVAL_OFFSET_MINUTES = 180L


class PackageRepositoryImpl(
    private val remoteDataSource: PackageRemoteDatasource,
    private val localDataSource: PackageDataSource,
    private val warehouseMap: Map<String, Warehouse>
) : BaseRepository(), PackageRepository {

    private val dataExceptionMapper = DataExceptionMapper()
    private val packages = mutableListOf<Package>()


    private var isLoaded = false


    override suspend fun getAllPackages(): Result<List<Package>> {

        return runCatching {
            if (isLoaded) {
                return@runCatching packages.toList()
            }
            val loadedPackages =
                runCatching {
                    remoteDataSource
                        .getAll()
                        .mapNotNull { dto -> mapRemotePackage(dto) }
                }.getOrElse {
                    localDataSource
                        .getPackages()
                        .mapNotNull { result -> result.rawData?.let { raw -> mapLocalPackage(raw) } }
                }
            packages.addAll(loadedPackages)
            isLoaded = true
            packages.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }


    private fun findWarehouse(
        warehouseId: String,
        packageId: String,
        type: String
    ) =

        warehouseMap[warehouseId]
            ?: throw NullRequiredFieldException(
                "Package '$packageId' $type warehouse not found."
            )


    override suspend fun getById(
        packageId: String
    ): Result<Package> {
        val cachedPackage = packages.firstOrNull {
            it.id == packageId
        }
        if (cachedPackage != null) {
            return Result.success(cachedPackage)
        }
        return runCatching {
            val dto = remoteDataSource
                .getById(packageId)
                ?: throw PackageNotFoundException()
            val packageModel =
                mapRemotePackage(dto) ?: throw PackageNotFoundException("Package '$packageId' mapping failed.")
            packages.add(packageModel)
            packageModel
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun getAllWarehouseStays():
            Result<List<PackageWarehouseStay>> {

        return getAllPackages()
            .mapCatching { packages ->
                packages.map {
                    createWarehouseStay(it)
                }
            }
    }


    private fun createWarehouseStay(
        cargoPackage: Package
    ): PackageWarehouseStay {

        return PackageWarehouseStay(
            packageId = cargoPackage.id,
            arrivedAt =
                LocalDateTime.now()
                    .minusHours(
                        Random.nextLong(
                            MIN_WAITING_HOURS,
                            MAX_WAITING_HOURS
                        )
                    )
        )
    }


    override suspend fun getAllDeliveryTimes():
            Result<List<PackageDeliveryTime>> {

        return getAllPackages()
            .mapCatching { packages ->
                packages.map {
                    createDeliveryTime(it)
                }
            }
    }


    private fun createDeliveryTime(
        cargoPackage: Package
    ): PackageDeliveryTime {

        val expectedArrival =
            Clock.System.now() +
                    Random.nextLong(
                        MIN_EXPECTED_HOURS,
                        MAX_EXPECTED_HOURS
                    ).hours


        val actualArrival =
            expectedArrival +
                    Random.nextLong(
                        MIN_ARRIVAL_OFFSET_MINUTES,
                        MAX_ARRIVAL_OFFSET_MINUTES
                    ).minutes


        return PackageDeliveryTime(
            packageId = cargoPackage.id,
            expectedArrivalTime = expectedArrival,
            actualArrivalTime = actualArrival
        )
    }


    override suspend fun getPackagesByWarehouseId(
        warehouseId: String
    ): Result<List<Package>> {

        return getAllPackages()
            .mapCatching { packages ->
                packages.filter {
                    it.originWarehouse.id == warehouseId
                }
            }
    }


    override suspend fun getAllPackageRequirements():
            Result<List<PackageRequirements>> {

        return getAllPackages()
            .mapCatching { packages ->
                packages.map {
                PackageRequirements(
                    packageId = it.id,
                    isFragile = Random.nextBoolean(),
                    requiresColdStorage = Random.nextBoolean(),
                    requiresSpecialHandling = Random.nextBoolean()
                )
            }
            }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun save(
        cargoPackage: Package
    ): Result<Package> {
        return runCatching {
            val request = cargoPackage.toCreateRequest()
            val dto = remoteDataSource.save(request)
            val packageModel = mapRemotePackage(dto)
                    ?: throw NullRequiredFieldException(
                        "Saved package '${dto.id}' mapping failed."
                    )
            packages.add(packageModel)
            packageModel
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        input: UpdatePackageInput
    ): Result<Package> {
        return runCatching {
            val request = input.toUpdateRequest()
            val dto = remoteDataSource
                    .update(
                        id = input.id,
                        request = request
                    )
            val updatedPackage =
                mapRemotePackage(dto)
                    ?: throw NullRequiredFieldException(
                        "Package '$input.id' update failed."
                    )


            packages.removeIf {
                it.id == input.id
            }
            packages.add(updatedPackage)
            updatedPackage
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun delete(
        id: String
    ): Result<String> {
        return runCatching {
            val deletedId = remoteDataSource.delete(id)
            packages.removeIf {
                it.id == deletedId
            }
            deletedId
        }.mapFailureToDomain(dataExceptionMapper)
    }

    private fun mapLocalPackage(raw: PackageRaw): Package? {
        return mapSafely(raw.id) {
            val originWarehouse = findWarehouse(raw.originHubId, raw.id, "origin")
            val destinationWarehouse = findWarehouse(raw.destinationHubId, raw.id, "destination")

            raw.toDomainModel(originWarehouse = originWarehouse, destinationWarehouse = destinationWarehouse)
        }
    }

    private fun mapRemotePackage(
        dto: PackageResponseDto
    ): Package? {

        return mapSafely(dto.id) {

            val originWarehouse =
                findWarehouse(
                    dto.originHubId,
                    dto.id,
                    "origin"
                )

            val destinationWarehouse =
                findWarehouse(
                    dto.destinationHubId,
                    dto.id,
                    "destination"
                )

            dto.toDomainModel(
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        }
    }
}

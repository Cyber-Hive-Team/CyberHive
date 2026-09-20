package org.example.data.repositoryImplementation

import org.example.data.exception.NullRequiredFieldException
import org.example.data.remote.dto.response.PackageResponseDto
import org.example.data.repositoryImplementation.dependencies.PackageRepositoryDependencies
import org.example.domain.model.Package
import org.example.domain.model.PackageRequirements
import org.example.domain.model.PackageWarehouseStay
import org.example.domain.model.Priority
import org.example.domain.model.input.PackageDeliveryTime
import org.example.domain.repository.PackageRepository
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes


private const val MIN_WAITING_HOURS = 1L
private const val MAX_WAITING_HOURS = 73L

private const val MIN_EXPECTED_HOURS = 2L
private const val MAX_EXPECTED_HOURS = 10L

private const val MIN_ARRIVAL_OFFSET_MINUTES = -60L
private const val MAX_ARRIVAL_OFFSET_MINUTES = 180L


class PackageRepositoryImpl(
    private val dependencies: PackageRepositoryDependencies
) : PackageRepository {


    private val warnings =
        mutableListOf<String>()


    private val packages =
        mutableListOf<Package>()


    private var isLoaded = false


    override suspend fun getAllPackages(): Result<List<Package>> {

        return runCatching {

            if (isLoaded) {
                return@runCatching packages.toList()
            }


            val loadedPackages =
                dependencies.remoteDataSource
                    .getAll()
                    .mapNotNull {
                        mapPackageSafely(it)
                    }


            packages.addAll(
                loadedPackages
            )


            isLoaded = true


            packages.toList()
        }
    }

    @Suppress("LongMethod")
    private fun mapPackageSafely(
        dto: PackageResponseDto
    ): Package? {

        return runCatching {

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


            dependencies.remoteValidator
                .validate(dto)


            dependencies.remoteMapper
                .mapToDomainModel(
                    dto = dto,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )

        }.getOrElse { exception ->

            if (exception is NullRequiredFieldException) {

                warnings.add(
                    "Package '${dto.id}': ${exception.message}"
                )

                null

            } else {

                throw exception
            }
        }
    }


    private fun findWarehouse(
        warehouseId: String,
        packageId: String,
        type: String
    ) =

        dependencies.warehouseMap[warehouseId]
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
        val dto =
            dependencies.remoteDataSource
                .getById(packageId)
                ?: error("Package with id '$packageId' was not found.")

        val packageModel =
            mapPackageSafely(dto) ?: error("Package mapping failed.")

            packages.add(packageModel)

            packageModel
        }
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
            .map { packages ->
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
    }
    }


    override suspend fun save(
        cargoPackage: Package
    ): Result<Package> {
        return runCatching {
            val request =
                dependencies.remoteMapper
                    .mapToCreateRequest(
                        id = cargoPackage.id,
                        weight = cargoPackage.weight,
                        priority = cargoPackage.priority,
                        originHubId = cargoPackage.originWarehouse.id,
                        destinationHubId = cargoPackage.destinationWarehouse.id
                    )
            val dto =
                dependencies.remoteDataSource
                    .save(request)
            val packageModel =
                mapPackageSafely(dto)
                    ?: cargoPackage
            packages.add(packageModel)
            packageModel
        }
    }


    override suspend fun update(
        id: String,
        weight: Double?,
        priority: Priority?,
        originHubId: String,
        destinationHubId: String
    ): Result<Package> {
        return runCatching {
            val request =
                dependencies.remoteMapper
                    .mapToUpdateRequest(
                        weight = weight,
                        priority = priority,
                        originHubId = originHubId,
                        destinationHubId = destinationHubId
                    )
            val dto =
                dependencies.remoteDataSource
                    .update(
                        id = id,
                        request = request
                    )
            val updatedPackage =
                mapPackageSafely(dto)
                    ?: throw NullRequiredFieldException(
                        "Package '$id' update failed."
                    )


            packages.removeIf {
                it.id == id
            }
            packages.add(updatedPackage)
            updatedPackage
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
                packages.removeIf {
                    it.id == id
                }
            }
            deleted
        }
    }
}

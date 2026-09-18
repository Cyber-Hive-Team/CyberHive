package org.example.data.repositoryImplementation

import org.example.data.dataholder.PackageRaw
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


    @Suppress("TooGenericExceptionCaught")
    override fun getAllPackages(): Result<List<Package>> {
        return runCatching {
            val rawResults = dependencies.localDataSource.getPackages()
            val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
            val rawPackages = rawResults.mapNotNull { it.rawData }
            mapPackages(rawPackages, warnings)
        }
    }



    private fun mapPackages(
        rawPackages: List<PackageRaw>,
        warnings: MutableList<String>
    ): List<Package> {


        return rawPackages.mapNotNull { raw ->


            val origin =
                dependencies.warehouseMap[
                    normalizeId(raw.originHubId)
                ]


            val destination =
                dependencies.warehouseMap[
                    normalizeId(raw.destinationHubId)
                ]


            val validation =
                dependencies.validator.validate(
                    raw,
                    origin,
                    destination
                )


            if (validation.isNotEmpty()) {

                warnings.addAll(validation)

                null

            } else {

                dependencies.localMapper.map(
                    raw,
                    origin!!,
                    destination!!
                )
            }
        }
    }


    private fun normalizeId(
        id: String
    ): String =
        id.trim().uppercase()


    override suspend fun getById(packageId: String): Package? {
        val remoteDto = dependencies.remoteDataSource.getById(packageId)
        if (remoteDto != null) {
            val originWarehouse =
                dependencies.warehouseRepository
                    .getById(remoteDto.originHubId)
            val destinationWarehouse =
                dependencies.warehouseRepository
                    .getById(remoteDto.destinationHubId)
            if (originWarehouse != null && destinationWarehouse != null) {
                return dependencies.remoteMapper.mapToDomainModel(
                    dto = remoteDto,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            }
        }

        return getAllPackages()
            .getOrNull()
            ?.find { it.id == packageId }
    }


    override fun getAllWarehouseStays():
            List<PackageWarehouseStay> {


        return getAllPackages()
            .getOrNull().orEmpty()
            .map { cargoPackage ->

                PackageWarehouseStay(
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
    }


    override fun getAllDeliveryTimes():
            List<PackageDeliveryTime> {


        return getAllPackages()
            .getOrNull().orEmpty()
            .map { cargoPackage ->


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


                PackageDeliveryTime(
                    packageId = cargoPackage.id,
                    expectedArrivalTime = expectedArrival,
                    actualArrivalTime = actualArrival
                )
            }
    }

    override fun getPackagesByWarehouseId(
        warehouseId: String
    ): Result<List<Package>> {
        return getAllPackages()
            .map { packages ->
                packages.filter {
                    it.originWarehouse.id == warehouseId
                }
            }
    }

    override fun getAllPackageRequirements():
            List<PackageRequirements> {

        return getAllPackages()
            .getOrNull()
            .orEmpty()
            .map { cargoPackage ->

                PackageRequirements(
                    packageId = cargoPackage.id,
                    isFragile = Random.nextBoolean(),
                    requiresColdStorage = Random.nextBoolean(),
                    requiresSpecialHandling = Random.nextBoolean()
                )
            }
    }

    override suspend fun save(
        cargoPackage: Package
    ): Package {


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



        return dependencies.remoteMapper
            .mapToDomainModel(
                dto = dto,
                originWarehouse = cargoPackage.originWarehouse,
                destinationWarehouse = cargoPackage.destinationWarehouse
            )
    }


    override suspend fun update(
        id: String,
        weight: Double?,
        priority: Priority?,
        originHubId: String,
        destinationHubId: String
    ): Package {
        val request = dependencies.remoteMapper
                .mapToUpdateRequest(
                    weight = weight,
                    priority = priority,
                    originHubId = originHubId,
                    destinationHubId = destinationHubId
                )
        val dto = dependencies.remoteDataSource.update(id = id, request = request)
        val originWarehouse =
            dependencies.warehouseRepository
                .getById(dto.originHubId)
        val destinationWarehouse =
            dependencies.warehouseRepository
                .getById(dto.destinationHubId)
        return if (originWarehouse != null && destinationWarehouse != null) {
            dependencies.remoteMapper.mapToDomainModel(
                dto = dto,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
        } else {
            requireNotNull(getById(id)) {
                "Package with id $id was not found"
            }
        }
    }


    override suspend fun delete(
        id: String
    ): Boolean {
        return dependencies.remoteDataSource
            .delete(id)
    }
}

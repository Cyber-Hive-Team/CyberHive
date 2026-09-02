package org.example.data.repository

import org.example.data.dataholder.PackageRaw
import org.example.data.datasource.PackageDataSource
import org.example.data.mapper.PackageMapper
import org.example.domain.model.Package
import org.example.domain.model.PackageWarehouseStay
import org.example.domain.model.Warehouse
import org.example.domain.model.input.PackageDeliveryTime
import org.example.domain.model.result.Result
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

class CsvPackageRepository(
    private val dataSource: PackageDataSource,
    private val mapper: PackageMapper,
    private val warehouseMap: Map<String, Warehouse>
) : PackageRepository {

    override fun getAllPackages(): Result<List<Package>> {
        val rawResults = dataSource.getPackages()
        val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
        val rawPackages = rawResults.mapNotNull { it.rawData }
        val packages = mapPackages(rawPackages = rawPackages, warnings = warnings)
        return Result(
            data = packages,
            errorMessage = warnings
                .takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )
        
    }

    private fun mapPackages(
        rawPackages: List<PackageRaw>,
        warnings: MutableList<String>
    ): List<Package> =
        rawPackages.mapNotNull { raw ->
            val origin = warehouseMap[normalizeId(raw.originHubId)]
            val destination = warehouseMap[normalizeId(raw.destinationHubId)]

            val validation = validate(raw, origin, destination)

            if (validation.isNotEmpty()) {
                warnings.addAll(validation)
                null
            } else {
                mapper.map(raw, origin!!, destination!!)
            }

        }

    private fun validate(
        raw: PackageRaw,
        origin: Warehouse?,
        destination: Warehouse?
    ): List<String> {
        val warnings = mutableListOf<String>()

        if (raw.id.isBlank()) {
            warnings.add("Warning: Package skipped - missing id")
        }

        if (origin == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "origin warehouse not found: ${raw.originHubId}"
            )
        }

        if (destination == null) {
            warnings.add(
                "Warning: Package ${raw.id} skipped - " +
                        "destination warehouse not found: ${raw.destinationHubId}"
            )
        }

        return warnings

    }

    private fun normalizeId(id: String): String =
        id.trim().uppercase()

    override fun getPackagesByWarehouseId(warehouseId: String): Result<List<Package>> {
        val result = getAllPackages()
        return Result(
            data = result.data.filter { cargoPackage ->
                cargoPackage.originWarehouse.id == warehouseId
            },
            errorMessage = result.errorMessage
        )

    }


    override fun getAllWarehouseStays(): List<PackageWarehouseStay> {

        return getAllPackages()
            .data
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


    override fun getAllDeliveryTimes(): List<PackageDeliveryTime> {

        return getAllPackages()
            .data
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

}


package org.example.data.repositoryImplementation

import org.example.domain.model.PackageDeliveryTime
import org.example.domain.repository.PackageDeliveryTimeRepository
import org.example.domain.repository.PackageRepository
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val MIN_EXPECTED_HOURS = 2L
private const val MAX_EXPECTED_HOURS = 10L

private const val MIN_ARRIVAL_OFFSET_MINUTES = -60L
private const val MAX_ARRIVAL_OFFSET_MINUTES = 180L


class GeneratedPackageDeliveryTimeRepository(

    private val packageRepository: PackageRepository
) : PackageDeliveryTimeRepository {

    override fun getAllDeliveryTimes(): List<PackageDeliveryTime> {

        return packageRepository
            .getAllPackages()
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

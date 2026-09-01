package org.example.data.repositoryImplementation

import org.example.domain.model.PackageWarehouseStay
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.PackageWarehouseStayRepository
import java.time.LocalDateTime
import kotlin.random.Random


private const val MIN_WAITING_HOURS = 1L
private const val MAX_WAITING_HOURS = 73L


class GeneratedPackageWarehouseStayRepository(
    private val packageRepository: PackageRepository
) : PackageWarehouseStayRepository {

    override fun getAllWarehouseStays(): List<PackageWarehouseStay> {

        return packageRepository
            .getAllPackages()
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
}

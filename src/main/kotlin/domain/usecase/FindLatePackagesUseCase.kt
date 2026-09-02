package org.example.domain.usecase

import org.example.domain.model.result.LatePackageResult
import org.example.domain.repository.PackageDeliveryTimeRepository

class FindLatePackagesUseCase(
    private val deliveryTimeRepository: PackageDeliveryTimeRepository
) {

    operator fun invoke(): List<LatePackageResult> {

        return deliveryTimeRepository
            .getAllDeliveryTimes()
            .filter { delivery ->
                delivery.actualArrivalTime >
                        delivery.expectedArrivalTime
            }
            .map { delivery ->

                val delayMinutes =
                    (delivery.actualArrivalTime -
                            delivery.expectedArrivalTime
                            ).inWholeMinutes

                LatePackageResult(
                    packageId = delivery.packageId,
                    delayMinutes = delayMinutes
                )
            }
            .sortedByDescending { result ->
                result.delayMinutes
            }
    }
}


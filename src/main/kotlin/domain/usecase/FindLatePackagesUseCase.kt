package org.example.domain.usecase

import org.example.domain.model.LatePackageResult
import org.example.domain.repository.PackageDeliveryTimeRepository
import java.time.Duration

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
                    Duration.between(
                        delivery.expectedArrivalTime,
                        delivery.actualArrivalTime
                    ).toMinutes()

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

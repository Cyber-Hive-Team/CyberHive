package org.example.domain.usecase

import org.example.domain.repository.PackageRepository
import java.time.Duration
import java.time.LocalDateTime

class FindPackagesWaitingTooLongUseCase(
    private val packageRepository: PackageRepository
) {

    operator fun invoke(
        maxWaitingHours: Long
    ): List<WaitingPackageResult> {

        val now = LocalDateTime.now()
        return packageRepository
            .getAllWarehouseStays()
            .map { stay ->
                val waitingHours =
                    Duration.between(
                        stay.arrivedAt,
                        now
                    ).toHours()

                WaitingPackageResult(
                    packageId = stay.packageId,
                    waitingHours = waitingHours
                )
            }
            .filter { result ->
                result.waitingHours > maxWaitingHours
            }
            .sortedByDescending { result ->
                result.waitingHours
            }
    }
}

data class WaitingPackageResult(
    val packageId: String,
    val waitingHours: Long
)

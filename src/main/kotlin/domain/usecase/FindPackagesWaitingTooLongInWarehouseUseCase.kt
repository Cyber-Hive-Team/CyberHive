package org.example.domain.usecase

import org.example.domain.model.exception.InvalidWaitingHoursException
import org.example.domain.model.result.WaitingPackageResult
import org.example.domain.repository.PackageRepository
import java.time.Duration
import java.time.LocalDateTime

private const val MIN_WAITING_HOURS = 0L

class FindPackagesWaitingTooLongInWarehouseUseCase(
    private val packageRepository: PackageRepository
) {

    operator fun invoke(
        maxWaitingHours: Long
    ): List<WaitingPackageResult> {
        validateWaitingHours(maxWaitingHours)
        val now = LocalDateTime.now()
        return packageRepository
            .getAllWarehouseStays()
            .map { stay ->
                val waitingHours =
                    Duration.between(
                        stay.arrivedAt,
                        now
                    ).toHours()
                WaitingPackageResult(packageId = stay.packageId, waitingHours = waitingHours)
            }
            .filter { result ->
                result.waitingHours > maxWaitingHours
            }
            .sortedByDescending { result ->
                result.waitingHours
            }
    }


    private fun validateWaitingHours(maxWaitingHours: Long) {
        if (maxWaitingHours < MIN_WAITING_HOURS) {
            throw InvalidWaitingHoursException()
        }

}
}


package org.example.domain.usecase

import org.example.domain.model.result.WaitingPackageResult
import org.example.domain.repository.PackageWarehouseStayRepository
import java.time.Duration
import java.time.LocalDateTime

class FindPackagesWaitingTooLongInWarehouseUseCase(
    private val warehouseStayRepository: PackageWarehouseStayRepository
) {

    operator fun invoke(
        maxWaitingHours: Long
    ): List<WaitingPackageResult> {

        val now = LocalDateTime.now()
        return warehouseStayRepository
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

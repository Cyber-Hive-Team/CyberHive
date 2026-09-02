package org.example.domain.usecase

import org.example.domain.model.PackageRequirements
import org.example.domain.model.WarehouseServices
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.PackageRequirementsRepository
import org.example.domain.repository.WarehouseRepository

class FindPackagesAtRiskOfDamageUseCase(
    private val packageRepository: PackageRepository,
    private val packageRequirementsRepository: PackageRequirementsRepository,
    private val warehouseRepository: WarehouseRepository
) {

    operator fun invoke(): List<DamageRiskResult> {
        val packageRequirements = packageRequirementsRepository.getAllPackageRequirements()
            .associateBy { requirement -> requirement.packageId }
        val warehouseServices = warehouseRepository.getAllWarehouseServices()
            .associateBy { services -> services.warehouseId }
        return packageRepository.getAllPackages().data.mapNotNull { cargoPackage ->
            val requirements = packageRequirements[cargoPackage.id]
                ?: return@mapNotNull null
            val warehouseId = cargoPackage.originWarehouse.id
            val services = warehouseServices[warehouseId]
                ?: return@mapNotNull null
            val reason = findRiskReason(requirements = requirements, services = services)
            if (reason == null) {
                null
            } else {
                DamageRiskResult(
                    packageId = cargoPackage.id,
                    warehouseId = warehouseId,
                    reason = reason
                )
            }
        }
    }

    private fun findRiskReason(
        requirements: PackageRequirements,
        services: WarehouseServices
    ): String? {

        return when {
            requirements.isFragile &&
                    !services.supportsFragileHandling ->
                "Fragile handling unavailable"

            requirements.requiresColdStorage &&
                    !services.supportsColdStorage ->
                "Cold storage unavailable"

            requirements.requiresSpecialHandling &&
                    !services.supportsSpecialHandling ->
                "Special handling unavailable"

            else -> null
        }
    }
}

data class DamageRiskResult(
    val packageId: String,
    val warehouseId: String,
    val reason: String
)

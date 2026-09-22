package org.example.domain.repository

import org.example.domain.model.Package
import org.example.domain.model.PackageRequirements
import org.example.domain.model.PackageWarehouseStay
import org.example.domain.model.Priority
import org.example.domain.model.input.PackageDeliveryTime

interface PackageRepository {
    suspend fun getAllPackages(): Result<List<Package>>
    suspend fun getPackagesByWarehouseId(warehouseId: String): Result<List<Package>>
    suspend fun getAllWarehouseStays(): Result<List<PackageWarehouseStay>>
    suspend fun getAllDeliveryTimes(): Result<List<PackageDeliveryTime>>
    suspend fun getAllPackageRequirements(): Result<List<PackageRequirements>>
    suspend fun getById(packageId: String): Result<Package>
    suspend fun save(cargoPackage: Package): Result<Package>

    suspend fun update(
        id: String,
        weight: Double? = null,
        priority: Priority? = null,
        originHubId: String,
        destinationHubId: String
    ): Result<Package>

    suspend fun delete(
        id: String
    ): Result<String>
}



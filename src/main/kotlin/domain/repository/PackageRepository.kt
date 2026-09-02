package org.example.domain.repository

import org.example.domain.model.Package
import org.example.domain.model.PackageWarehouseStay
import org.example.domain.model.result.Result

interface PackageRepository {
    fun getAllPackages(): Result<List<Package>>
    fun getPackagesByWarehouseId(warehouseId: String): Result<List<Package>>
    fun getAllWarehouseStays(): List<PackageWarehouseStay>

}


package org.example.domain.repository

import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.result.Result

interface WarehouseRepository {
    fun getAllWarehouses(): Result<List<Warehouse>>
    suspend fun getById(warehouseId: String): Warehouse?
    suspend fun addPackageToCargoQueue(warehouseId: String, cargoPackage: Package): Boolean
    suspend fun sortCargoQueue(warehouseId: String): Boolean
    suspend fun isPackageInCargoQueue(warehouseId: String, packageId: String): Boolean
    fun getAllWarehouseServices(): List<WarehouseServices>
    suspend fun save(warehouse: Warehouse): Warehouse
    suspend fun update(
        id: String, name: String? = null,
        regionalZone: RegionalZone? = null, latitude: Double? = null,
        longitude: Double? = null
    ): Warehouse

    suspend fun delete(id: String): Boolean
}

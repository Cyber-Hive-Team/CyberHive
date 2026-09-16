package org.example.domain.repository

import org.example.domain.model.Package
import org.example.domain.model.RegionalZone
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.result.Result

interface WarehouseRepository {
    fun getAllWarehouses(): Result<List<Warehouse>>
    fun getWarehouseById(warehouseId: String): Warehouse?
    fun addPackageToCargoQueue(warehouseId: String, cargoPackage: Package): Boolean
    fun sortCargoQueue(warehouseId: String): Boolean
    fun isPackageInCargoQueue(warehouseId: String, packageId: String): Boolean
    fun getAllWarehouseServices(): List<WarehouseServices>
    suspend fun getById(id: String): Warehouse
    suspend fun save(warehouse: Warehouse): Warehouse
    suspend fun update(
        id: String, name: String? = null,
        regionalZone: RegionalZone? = null, latitude: Double? = null,
        longitude: Double? = null
    ): Warehouse

    suspend fun delete(id: String): Boolean
}

package org.example.domain.repository

import org.example.domain.model.Package
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

}

package org.example.domain.repository

import org.example.domain.model.Package
import org.example.domain.model.Warehouse
import org.example.domain.model.WarehouseServices
import org.example.domain.model.input.UpdateWarehouseInput
import org.example.domain.model.WarehouseStatus

@Suppress("TooManyFunctions")
interface WarehouseRepository {
    suspend fun getAllWarehouses(): Result<List<Warehouse>>
    suspend fun getById(id: String): Result<Warehouse>
    suspend fun addPackageToCargoQueue(warehouseId: String, cargoPackage: Package): Result<Boolean>
    suspend fun sortCargoQueue(warehouseId: String): Result<Boolean>
    suspend fun isPackageInCargoQueue(warehouseId: String, packageId: String): Result<Boolean>
    suspend fun getAllWarehouseServices(): Result<List<WarehouseServices>>
    suspend fun save(warehouse: Warehouse): Result<Warehouse>
    suspend fun update(input: UpdateWarehouseInput): Result<Warehouse>

    suspend fun delete(id: String): Result<String>

    suspend fun getStatus(warehouseId: String): Result<WarehouseStatus>
    suspend fun updateStatus(warehouseId: String, status: WarehouseStatus): Result<Boolean>
}

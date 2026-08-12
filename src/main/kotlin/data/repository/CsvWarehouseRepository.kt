package org.example.data.repository

import org.example.data.dataparsing.parseWarehouse
import org.example.domain.model.Warehouse
import org.example.domain.repository.WarehouseRepository

class CsvWarehouseRepository(private val filePath: String) : WarehouseRepository {
    override fun getAllWarehouses(): List<Warehouse> {
        return parseWarehouse(filePath).map { raw ->
            Warehouse(
                id = raw.id,
                name = raw.name,
                regionalZone = raw.regionalZone,
                latitude = raw.latitude,
                longitude = raw.longitude
            )
        }
    }
}
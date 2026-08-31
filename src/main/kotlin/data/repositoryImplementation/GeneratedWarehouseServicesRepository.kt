package org.example.data.repositoryImplementation

import org.example.domain.model.WarehouseServices
import org.example.domain.repository.WarehouseRepository
import org.example.domain.repository.WarehouseServicesRepository
import kotlin.random.Random

class GeneratedWarehouseServicesRepository(
    private val warehouseRepository: WarehouseRepository
) : WarehouseServicesRepository {

    override fun getAllWarehouseServices():
            List<WarehouseServices> {

        return warehouseRepository
            .getAllWarehouses()
            .data
            .map { warehouse ->
                WarehouseServices(
                    warehouseId = warehouse.id,
                    supportsFragileHandling = Random.nextBoolean(),
                    supportsColdStorage = Random.nextBoolean(),
                    supportsSpecialHandling = Random.nextBoolean()
                )
            }
    }
}
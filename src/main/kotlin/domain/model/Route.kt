package org.example.domain.model

import org.example.domain.model.exception.InvalidRouteIdException
import org.example.domain.model.exception.SameWarehouseException

private const val ROUTE_ID_PREFIX = "^RT-\\d{5}$"

data class Route(
    val id: String,
    val distanceKm: Double,
    val typicalDelayMin: Int,
    val originWarehouse: Warehouse,
    val destinationWarehouse: Warehouse
){

    init {
        validateId()
        validateDifferentWarehouses()
    }

    private fun validateId() {
        if (!id.matches(Regex(ROUTE_ID_PREFIX))) {
            throw InvalidRouteIdException()
        }
    }

    private fun validateDifferentWarehouses() {
        if (originWarehouse.id == destinationWarehouse.id) {
            throw SameWarehouseException()
        }
    }

}

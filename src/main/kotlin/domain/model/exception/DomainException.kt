package org.example.domain.model.exception

open class DomainException(message: String) : Exception(message) {

    companion object {
        const val WAREHOUSE_NOT_FOUND = "WAREHOUSE_NOT_FOUND"
        const val INVALID_PACKAGE_WEIGHT = "INVALID_PACKAGE_WEIGHT"
        const val INVALID_VEHICLE_CAPACITY = "INVALID_VEHICLE_CAPACITY"
    }
}

class WarehouseNotFoundException(message: String = WAREHOUSE_NOT_FOUND) : DomainException(message)
class InvalidPackageWeightException(message: String = INVALID_PACKAGE_WEIGHT) : DomainException(message)
class InvalidVehicleCapacityException(message: String = INVALID_VEHICLE_CAPACITY) : DomainException(message)

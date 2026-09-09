package org.example.domain.model.exception

open class DomainException(message: String) : Exception(message) {

    companion object {
        const val WAREHOUSE_NOT_FOUND = "Warehouse not found."
        const val VEHICLE_NOT_FOUND = "Vehicle not found."
        const val PACKAGE_NOT_FOUND = "Package not found."
        const val INVALID_LIMIT = "Limit must be greater than zero."
        const val ROUTE_NOT_FOUND = "No route found."
        const val INVALID_REQUIRED_WEIGHT = "Required weight must be greater than zero."
        const val INVALID_PACKAGE_COUNT = "Package count must be greater than zero."
    }
}

class WarehouseNotFoundException(message: String = WAREHOUSE_NOT_FOUND) : DomainException(message)
class VehicleNotFoundException(message: String = VEHICLE_NOT_FOUND) : DomainException(message)
class PackageNotFoundException(message: String = PACKAGE_NOT_FOUND) : DomainException(message)
class InvalidLimitException(message: String = INVALID_LIMIT) : DomainException(message)
class RouteNotFoundException(message: String = ROUTE_NOT_FOUND) : DomainException(message)
class InvalidRequiredWeightException(message: String = INVALID_REQUIRED_WEIGHT) : DomainException(message)
class InvalidPackageCountException(message: String = INVALID_PACKAGE_COUNT) : DomainException(message)

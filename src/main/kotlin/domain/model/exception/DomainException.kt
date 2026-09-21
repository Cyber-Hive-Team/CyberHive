package org.example.domain.model.exception

open class DomainException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    companion object {
        const val WAREHOUSE_NOT_FOUND = "Warehouse not found."
        const val VEHICLE_NOT_FOUND = "Vehicle not found."
        const val PACKAGE_NOT_FOUND = "Package not found."
        const val INVALID_LIMIT = "Limit must be greater than zero."
        const val ROUTE_NOT_FOUND = "No route found."
        const val INVALID_REQUIRED_WEIGHT = "Required weight must be greater than zero."
        const val INVALID_PACKAGE_COUNT = "Package count must be greater than zero."
        const val FAILED_TO_EXECUTE_OR_UNDO_COMMAND ="Failed_to_execute_or_undo_command"
        const val INVALID_PACKAGE_WEIGHT = "INVALID_PACKAGE_WEIGHT"
        const val INVALID_VEHICLE_CAPACITY = "INVALID_VEHICLE_CAPACITY"
        const val INVALID_WAITING_HOURS = "INVALID_WAITING_HOURS"
        const val VEHICLE_REASSIGNMENT_FAILED = "Vehicle reassignment failed"
        const val INVALID_PACKAGE_ID = "Invalid package ID."
        const val INVALID_ROUTE_ID = "Invalid route ID."
        const val INVALID_VEHICLE_ID = "Invalid vehicle ID."
        const val INVALID_WAREHOUSE_ID = "Invalid warehouse ID."
        const val INVALID_BASE_RATE = "Base rate cannot be negative."
        const val INVALID_DISTANCE = "Distance must be greater than zero."
        const val INVALID_DELAY = "Typical delay cannot be negative."
        const val INVALID_COST_PER_KM = "Cost per kilometer cannot be negative."
        const val INVALID_WAREHOUSE_NAME = "Warehouse name cannot be empty."
        const val INVALID_LATITUDE = "Latitude must be between -90 and 90."
        const val INVALID_LONGITUDE = "Longitude must be between -180 and 180."
        const val SAME_WAREHOUSE = "Origin and destination warehouses cannot be the same."
        const val NO_UPDATE_FIELDS ="At least one field must be provided for update."
        const val INVALID_ORIGIN_WAREHOUSE = "Origin warehouse ID cannot be empty."
        const val INVALID_DESTINATION_WAREHOUSE = "Destination warehouse ID cannot be empty."
        const val INVALID_CURRENT_HUB = "Current hub ID cannot be empty."
        const val NETWORK_ERROR = "Unable to connect to the remote service."
        const val DATA_ACCESS_FAILED = "Failed to access data."
        const val INVALID_DATA = "The received data is invalid."
        const val UNKNOWN_ERROR = "An unexpected error occurred."

    }
}

class WarehouseNotFoundException(message: String = WAREHOUSE_NOT_FOUND) : DomainException(message)
class VehicleNotFoundException(message: String = VEHICLE_NOT_FOUND) : DomainException(message)
class InvalidLimitException(message: String = INVALID_LIMIT) : DomainException(message)
class RouteNotFoundException(message: String = ROUTE_NOT_FOUND) : DomainException(message)
class InvalidRequiredWeightException(message: String = INVALID_REQUIRED_WEIGHT) : DomainException(message)
class InvalidPackageCountException(message: String = INVALID_PACKAGE_COUNT) : DomainException(message)
class PackageNotFoundException(message: String = PACKAGE_NOT_FOUND) : DomainException(message)
class CommandExecutionException(message: String = FAILED_TO_EXECUTE_OR_UNDO_COMMAND) : DomainException(message)
class InvalidPackageWeightException(message: String = INVALID_PACKAGE_WEIGHT) : DomainException(message)
class InvalidVehicleCapacityException(message: String = INVALID_VEHICLE_CAPACITY) : DomainException(message)
class InvalidWaitingHoursException(message: String = INVALID_WAITING_HOURS) : DomainException(message)
class VehicleReassignmentFailedException(message: String = VEHICLE_REASSIGNMENT_FAILED) : DomainException(message)
class InvalidPackageIdException(message: String = INVALID_PACKAGE_ID) : DomainException(message)
class InvalidRouteIdException(message: String = INVALID_ROUTE_ID) : DomainException(message)
class InvalidVehicleIdException(message: String = INVALID_VEHICLE_ID) : DomainException(message)
class InvalidWarehouseIdException(message: String = INVALID_WAREHOUSE_ID) : DomainException(message)
class SameWarehouseException(message: String = SAME_WAREHOUSE) : DomainException(message)
class EntityValidationException(message: String, cause: Throwable? = null) : DomainException(message, cause)
class NetworkException(message: String = NETWORK_ERROR, cause: Throwable? = null) : DomainException(message, cause)
class DataAccessException(message: String = DATA_ACCESS_FAILED, cause: Throwable? = null) :
    DomainException(message, cause)

class InvalidDataException(message: String = INVALID_DATA, cause: Throwable? = null) : DomainException(message, cause)
class UnknownException(message: String = UNKNOWN_ERROR, cause: Throwable? = null) : DomainException(message, cause)

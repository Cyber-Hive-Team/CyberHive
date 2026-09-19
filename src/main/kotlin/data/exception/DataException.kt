package org.example.data.exception

open class DataException(message: String) : Exception(message) {

    companion object {
        const val FILE_NOT_FOUND = "Data file not found."
        const val EMPTY_FILE = "Data file is empty."
        // const val INVALID_ROW_FORMAT = "Row has an invalid format."
        const val INVALID_COLUMN_COUNT = "Row does not have the expected number of columns."
        const val MISSING_REQUIRED_FIELD = "Row is missing a required field."
        // const val MAPPING_FAILED = "Failed to map raw data to domain model."
        // const val REMOTE_REQUEST_FAILED = "Remote request failed."
        // const val INVALID_RESPONSE = "Received an invalid or unexpected response."
        // const val UNAUTHORIZED = "Request was not authorized. Check your API key/token."
        // const val PARSING_FAILED = "Failed to parse data."

        const val INVALID_PACKAGE_ID = "Invalid package ID."
        const val INVALID_PACKAGE_WEIGHT = "Package weight must be greater than zero."

        const val INVALID_ORIGIN_WAREHOUSE_ID = "Origin warehouse ID cannot be empty."
        const val INVALID_DESTINATION_WAREHOUSE_ID = "Destination warehouse ID cannot be empty."
        const val ORIGIN_WAREHOUSE_NOT_FOUND = "Origin warehouse was not found."
        const val DESTINATION_WAREHOUSE_NOT_FOUND = "Destination warehouse was not found."
        const val SAME_WAREHOUSE = "Origin and destination warehouses cannot be the same."

        const val INVALID_ROUTE_ID = "Invalid route ID."
        const val INVALID_DISTANCE = "Distance must be greater than zero."
        const val INVALID_DELAY = "Typical delay cannot be negative."

        const val INVALID_VEHICLE_ID = "Invalid vehicle ID."
        const val INVALID_CURRENT_HUB = "Current warehouse ID cannot be empty."
        const val CURRENT_HUB_NOT_FOUND = "Current warehouse was not found."
        const val INVALID_VEHICLE_CAPACITY = "Max capacity must be greater than zero."
        const val INVALID_COST_PER_KM = "Cost per kilometer cannot be negative."

        const val INVALID_WAREHOUSE_ID = "Invalid warehouse ID."
        const val INVALID_WAREHOUSE_NAME = "Warehouse name cannot be empty."
        const val INVALID_LATITUDE = "Latitude must be between -90 and 90."
        const val MISSING_LATITUDE = "Latitude is required."
        const val INVALID_LONGITUDE = "Longitude must be between -180 and 180."
        const val MISSING_LONGITUDE = "Longitude is required."
       const val NULL_REQUIRED_FIELD = "Required field cannot be null."
    }
}

class NullRequiredFieldException(message: String = NULL_REQUIRED_FIELD) : DataException(message)
class FileNotFoundDataException(message: String = FILE_NOT_FOUND) : DataException(message)
class EmptyFileDataException(message: String = EMPTY_FILE) : DataException(message)
// class InvalidRowFormatException(message: String = INVALID_ROW_FORMAT) : DataException(message)
class InvalidColumnCountException(message: String = INVALID_COLUMN_COUNT) : DataException(message)
class MissingRequiredFieldException(message: String = MISSING_REQUIRED_FIELD) : DataException(message)
// class MappingFailedException(message: String = MAPPING_FAILED) : DataException(message)
// class RemoteRequestFailedException(message: String = REMOTE_REQUEST_FAILED) : DataException(message)
// class InvalidResponseException(message: String = INVALID_RESPONSE) : DataException(message)
// class UnauthorizedDataException(message: String = UNAUTHORIZED) : DataException(message)
// class ParsingFailedException(message: String = PARSING_FAILED) : DataException(message)

class InvalidPackageIdDataException(message: String = INVALID_PACKAGE_ID) : DataException(message)
class InvalidPackageWeightDataException(message: String = INVALID_PACKAGE_WEIGHT) : DataException(message)

class InvalidOriginWarehouseIdDataException(message: String = INVALID_ORIGIN_WAREHOUSE_ID) : DataException(message)
class InvalidDestinationWarehouseIdDataException(message: String = INVALID_DESTINATION_WAREHOUSE_ID) : DataException(message)
class OriginWarehouseNotFoundDataException(message: String = ORIGIN_WAREHOUSE_NOT_FOUND) : DataException(message)
class DestinationWarehouseNotFoundDataException(message: String = DESTINATION_WAREHOUSE_NOT_FOUND) : DataException(message)
class SameWarehouseDataException(message: String = SAME_WAREHOUSE) : DataException(message)

class InvalidRouteIdDataException(message: String = INVALID_ROUTE_ID) : DataException(message)
class InvalidDistanceDataException(message: String = INVALID_DISTANCE) : DataException(message)
class InvalidDelayDataException(message: String = INVALID_DELAY) : DataException(message)

class InvalidVehicleIdDataException(message: String = INVALID_VEHICLE_ID) : DataException(message)
class InvalidCurrentHubDataException(message: String = INVALID_CURRENT_HUB) : DataException(message)
class CurrentHubNotFoundDataException(message: String = CURRENT_HUB_NOT_FOUND) : DataException(message)
class InvalidVehicleCapacityDataException(message: String = INVALID_VEHICLE_CAPACITY) : DataException(message)
class InvalidCostPerKmDataException(message: String = INVALID_COST_PER_KM) : DataException(message)

class InvalidWarehouseIdDataException(message: String = INVALID_WAREHOUSE_ID) : DataException(message)
class InvalidWarehouseNameDataException(message: String = INVALID_WAREHOUSE_NAME) : DataException(message)
class MissingLatitudeDataException(message: String = MISSING_LATITUDE) : DataException(message)
class InvalidLatitudeDataException(message: String = INVALID_LATITUDE) : DataException(message)
class MissingLongitudeDataException(message: String = MISSING_LONGITUDE) : DataException(message)
class InvalidLongitudeDataException(message: String = INVALID_LONGITUDE) : DataException(message)

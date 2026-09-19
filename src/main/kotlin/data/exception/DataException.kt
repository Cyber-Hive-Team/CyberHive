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
       const val NULL_REQUIRED_FIELD = "Required field cannot be null."
    }
}

class NullRequiredFieldException(message: String = NULL_REQUIRED_FIELD) : DataException(message)
class FileNotFoundDataException(message: String = FILE_NOT_FOUND) : DataException(message)
class EmptyFileDataException(message: String = EMPTY_FILE) : DataException(message)
//class InvalidRowFormatException(message: String = INVALID_ROW_FORMAT) : DataException(message)
class InvalidColumnCountException(message: String = INVALID_COLUMN_COUNT) : DataException(message)
class MissingRequiredFieldException(message: String = MISSING_REQUIRED_FIELD) : DataException(message)
//class MappingFailedException(message: String = MAPPING_FAILED) : DataException(message)
//class RemoteRequestFailedException(message: String = REMOTE_REQUEST_FAILED) : DataException(message)
//class InvalidResponseException(message: String = INVALID_RESPONSE) : DataException(message)
//class UnauthorizedDataException(message: String = UNAUTHORIZED) : DataException(message)
//class ParsingFailedException(message: String = PARSING_FAILED) : DataException(message)

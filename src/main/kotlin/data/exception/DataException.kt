package org.example.data.exception

open class DataException(message: String) : Exception(message) {

    companion object {
        const val FILE_NOT_FOUND = "Data file not found."
        const val EMPTY_FILE = "Data file is empty."
        const val INVALID_COLUMN_COUNT = "Row does not have the expected number of columns."
        const val MISSING_REQUIRED_FIELD = "Row is missing a required field."
       const val NULL_REQUIRED_FIELD = "Required field cannot be null."
    }
}

class NullRequiredFieldException(message: String = NULL_REQUIRED_FIELD) : DataException(message)
class FileNotFoundDataException(message: String = FILE_NOT_FOUND) : DataException(message)
class EmptyFileDataException(message: String = EMPTY_FILE) : DataException(message)
class InvalidColumnCountException(message: String = INVALID_COLUMN_COUNT) : DataException(message)
class MissingRequiredFieldException(message: String = MISSING_REQUIRED_FIELD) : DataException(message)

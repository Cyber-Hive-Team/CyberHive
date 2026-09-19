package org.example.data.validation

sealed class DataValidationResult {

    data object Success : DataValidationResult()

    data class Failure(
        val violations: List<DataFieldViolation>
    ) : DataValidationResult()
}

data class DataFieldViolation(
    val field: String,
    val message: String
)

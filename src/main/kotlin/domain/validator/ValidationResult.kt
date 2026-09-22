package org.example.domain.validator

sealed class ValidationResult {

    data object Success : ValidationResult()

    data class Failure(
        val violations: List<FieldViolation>
    ) : ValidationResult()
}

data class FieldViolation(
    val field: FieldError,
    val message: String
)

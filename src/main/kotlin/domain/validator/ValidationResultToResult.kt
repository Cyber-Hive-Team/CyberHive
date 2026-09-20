package org.example.domain.validator

fun List<FieldViolation>.toResult(): ValidationResult {
    return if (isEmpty()) {
        ValidationResult.Success
    } else {
        ValidationResult.Failure(this)
    }
}

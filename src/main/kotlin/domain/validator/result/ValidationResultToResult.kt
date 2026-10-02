package org.example.domain.validator.result

fun List<FieldViolation>.toResult(): ValidationResult {
    return if (isEmpty()) {
        ValidationResult.Success
    } else {
        ValidationResult.Failure(this)
    }
}

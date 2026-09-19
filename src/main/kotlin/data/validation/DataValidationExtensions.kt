package org.example.data.validation

fun List<DataFieldViolation>.toResult(): DataValidationResult {
    return if (isEmpty()) {
        DataValidationResult.Success
    } else {
        DataValidationResult.Failure(this)
    }
}

package org.example.domain.validator

import org.example.domain.validator.result.ValidationResult

interface Validator<T, U> {
    fun validateCreate(entity: T): ValidationResult
    fun validateUpdate(input: U): ValidationResult

}

package org.example.domain.validator

interface Validator<T, U> {
    fun validateId(id: String): ValidationResult
    fun validateCreate(entity: T): ValidationResult
    fun validateUpdate(input: U): ValidationResult

}

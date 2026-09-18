package org.example.domain.model.exception

import org.example.domain.validator.FieldViolation

class ValidationException(
    val violations: List<FieldViolation>
) : DomainException("Validation failed.")

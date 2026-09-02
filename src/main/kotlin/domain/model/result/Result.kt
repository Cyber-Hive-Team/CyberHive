package org.example.domain.model.result

data class Result<T>(
    val data: T,
    val errorMessage: String? = null
)

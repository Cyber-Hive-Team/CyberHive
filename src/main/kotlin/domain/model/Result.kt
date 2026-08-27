package org.example.domain.model

data class Result<T>(
    val data: T,
    val errorMessage: String? = null
)
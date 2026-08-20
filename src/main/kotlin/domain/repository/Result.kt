package org.example.domain.repository

data class Result<T>(
    val data: T,
    val errorMessage: String? = null
)
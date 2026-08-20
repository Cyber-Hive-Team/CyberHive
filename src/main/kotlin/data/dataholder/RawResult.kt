package org.example.data.dataholder

data class RawResult<T>(
    val rawData: T,
    val errorMessage: String? = null
)

package org.example.domain.builder

import org.example.domain.model.Warehouse

data class BuildResult(
    val success: List<Warehouse>,
    val errors: List<String>
)
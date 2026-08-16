package org.example.data.dataholder

import org.example.domain.model.Package

data class PackageMappingResult(
    val packageItem: Package?,
    val warnings: List<String>
)
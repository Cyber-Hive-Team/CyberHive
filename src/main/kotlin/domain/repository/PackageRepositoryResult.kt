package org.example.domain.repository

import org.example.domain.model.Package

data class PackageRepositoryResult(
    val packages: List<Package>,
    val warnings: List<String>
)
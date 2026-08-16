package org.example.domain.repository

import org.example.domain.model.Package

interface PackageRepository {
    fun getAllPackages(): Result<List<Package>>
}


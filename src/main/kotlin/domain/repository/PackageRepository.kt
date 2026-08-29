package org.example.domain.repository

import org.example.domain.model.Package
import org.example.domain.model.Result

interface PackageRepository {
    fun getAllPackages(): Result<List<Package>>
}


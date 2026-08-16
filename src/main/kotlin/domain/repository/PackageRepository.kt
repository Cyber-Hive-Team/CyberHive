package org.example.domain.repository

interface PackageRepository {
    fun getAllPackages(): PackageRepositoryResult
}

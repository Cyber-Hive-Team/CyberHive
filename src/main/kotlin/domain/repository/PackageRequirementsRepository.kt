package org.example.domain.repository

import org.example.domain.model.PackageRequirements

interface PackageRequirementsRepository {
    fun getAllPackageRequirements(): List<PackageRequirements>
}
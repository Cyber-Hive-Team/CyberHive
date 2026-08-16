package org.example.data.repository

import org.example.data.datasource.PackageDataSource
import org.example.data.mapper.PackageMapper
import org.example.domain.model.Package
import org.example.domain.repository.PackageRepository
import org.example.domain.repository.PackageRepositoryResult

class CsvPackageRepository(
    private val dataSource: PackageDataSource,
    private val mapper: PackageMapper
) : PackageRepository {


    override fun getAllPackages(): PackageRepositoryResult {
        val dataSourceResult = dataSource.getPackages()

        val packages = mutableListOf<Package>()
        val warnings = dataSourceResult.warnings.toMutableList()

        for (rawPackage in dataSourceResult.packages) {
            val mappingResult = mapper.map(rawPackage)

            warnings.addAll(mappingResult.warnings)

            mappingResult.packageItem?.let { packageItem ->
                packages.add(packageItem)
            }
        }

        return PackageRepositoryResult(
            packages = packages,
            warnings = warnings
        )
    }
}
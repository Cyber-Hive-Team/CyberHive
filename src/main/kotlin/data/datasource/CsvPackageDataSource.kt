package org.example.data.datasource

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parsePackages

class CsvPackageDataSource(
    private val filePath: String
) : PackageDataSource {

    override fun getPackages(): List<RawResult<PackageRaw>> {
        return parsePackages(filePath)
    }
}
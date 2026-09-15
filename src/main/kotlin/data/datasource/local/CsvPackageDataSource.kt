package org.example.data.datasource.local

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RawResult
import org.example.data.dataparsing.parsePackages
import org.example.data.datasource.PackageDataSource

class CsvPackageDataSource(
    private val filePath: String
) : PackageDataSource {

    override fun getPackages(): List<RawResult<PackageRaw>> {
        return parsePackages(filePath)
    }

}

package org.example.data.datasource

import org.example.data.dataholder.PackageParseResult
import org.example.data.dataparsing.parsePackages

class CsvPackageDataSource(
    private val filePath: String
) : PackageDataSource {

    override fun getPackages(): PackageParseResult {
        return parsePackages(filePath)
    }
}
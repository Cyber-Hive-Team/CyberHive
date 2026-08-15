package org.example.data.datasource

import org.example.data.dataholder.PackageParseResult

interface PackageDataSource {
    fun getPackages(): PackageParseResult
}
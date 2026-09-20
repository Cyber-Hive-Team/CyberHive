package org.example.data.datasource

import org.example.data.dataholder.PackageRaw
import org.example.data.dataholder.RawResult

interface PackageDataSource {
    fun getPackages(): List<RawResult<PackageRaw>>
}
package org.example.data.datasource

import org.example.data.datasource.local.model.PackageLocalData
import org.example.data.dataholder.RawResult

interface PackageDataSource {
    fun getPackages(): List<RawResult<PackageLocalData>>
}

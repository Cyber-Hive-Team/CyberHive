package org.example.data.datasource

import org.example.data.datasource.local.model.PackageLocalData
import org.example.data.dataholder.RawResult
import org.example.data.dataholder.PackageRaw

interface PackageDataSource {
    suspend  fun getPackages(): List<RawResult<PackageLocalData>>
    suspend  fun replaceAll(packages: List<PackageRaw>)
}

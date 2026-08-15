package org.example.data.dataholder

data class PackageParseResult(
    val packages: List<PackageRaw>,
    val warnings: List<String>
)
package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.RouteRaw
import org.example.data.dataparsing.parseRoutes

class CsvRouteDataSource(
    private val filePath: String
) : RouteDataSource {

    override fun getRoutes(): List<RawResult<RouteRaw>> {
        return parseRoutes(filePath)
    }
}
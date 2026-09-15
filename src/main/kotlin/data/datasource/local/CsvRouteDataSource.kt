package org.example.data.datasource.local

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.RouteRaw
import org.example.data.dataparsing.parseRoutes
import org.example.data.datasource.RouteDataSource

class CsvRouteDataSource(
    private val filePath: String
) : RouteDataSource {

    override fun getRoutes(): List<RawResult<RouteRaw>> {
        return parseRoutes(filePath)
    }


}

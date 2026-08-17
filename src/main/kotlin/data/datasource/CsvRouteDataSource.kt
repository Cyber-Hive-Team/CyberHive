package org.example.data.datasource

import org.example.data.dataparsing.parseRoutes

class CsvRouteDataSource(
    private val filePath: String
) : RouteDataSource {

    override fun getRoutes(): RouteDataSourceResult {
        val result = parseRoutes(filePath)
        return RouteDataSourceResult(
            routes = result.routes,
            warnings = result.warnings
        )
    }
}
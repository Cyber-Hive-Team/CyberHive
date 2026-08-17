package org.example.data.datasource

import org.example.data.dataholder.RouteRaw

data class RouteDataSourceResult(
    val routes: List<RouteRaw>,
    val warnings: List<String>
)
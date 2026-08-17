package org.example.data.dataholder

data class RouteParseResult(
    val routes: List<RouteRaw>,
    val warnings: List<String>
)
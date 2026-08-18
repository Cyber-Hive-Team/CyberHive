package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.dataholder.RouteRaw

interface RouteDataSource {

    fun getRoutes(): List<RawResult<RouteRaw>>
}
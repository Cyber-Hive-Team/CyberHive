package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.datasource.local.model.RouteLocalData
import org.example.data.dataholder.RouteRaw

interface RouteDataSource {

    suspend fun getRoutes(): List<RawResult<RouteLocalData>>
    suspend fun replaceAll(routes: List<RouteRaw>)
}

package org.example.data.datasource

import org.example.data.dataholder.RawResult
import org.example.data.datasource.local.model.RouteLocalData

interface RouteDataSource {

    fun getRoutes(): List<RawResult<RouteLocalData>>
}

package org.example.domain.repository

import org.example.domain.model.Route
import org.example.domain.model.result.Result

interface RouteRepository {
    fun getAllRoutes(): Result<List<Route>>
    suspend fun getRemoteById(routeId: String): Route?
    suspend fun save(route: Route): Route
    suspend fun update(route: Route): Route
    suspend fun delete(id: String): Boolean
}

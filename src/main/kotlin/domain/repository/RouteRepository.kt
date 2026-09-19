package org.example.domain.repository

import org.example.domain.model.Route

interface RouteRepository {
    suspend fun getAllRoutes(): Result<List<Route>>
    suspend fun getById(routeId: String): Route?
    suspend fun save(route: Route): Route
    suspend fun update(route: Route): Route
    suspend fun delete(id: String): Boolean
}

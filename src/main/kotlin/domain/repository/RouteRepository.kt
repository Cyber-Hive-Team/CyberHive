package org.example.domain.repository

import org.example.domain.model.Route


interface RouteRepository {
    suspend fun getAllRoutes(): Result<List<Route>>
    suspend fun getById(routeId: String): Result<Route>
    suspend fun save(route: Route): Result<Route>
    suspend fun update(route: Route): Result<Route>
    suspend fun delete(id: String): Result<String>
}

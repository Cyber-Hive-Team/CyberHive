package org.example.domain.repository

import org.example.domain.model.Route

interface RouteRepository {
    fun getAllRoutes(): List<Route>
}
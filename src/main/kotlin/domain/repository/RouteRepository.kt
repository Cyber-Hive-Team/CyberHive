package org.example.domain.repository

import org.example.domain.model.result.Result
import org.example.domain.model.Route

interface RouteRepository {
    fun getAllRoutes(): Result<List<Route>>
}

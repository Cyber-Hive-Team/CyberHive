package org.example.data.repositoryImplementation

import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.remote.RouteRemoteDataSource
import org.example.data.exception.NullRequiredFieldException
import org.example.data.mapper.DataExceptionMapper
import org.example.data.mapper.csv.toDomainModel
import org.example.data.mapper.mapFailureToDomain
import org.example.data.mapper.remote.toCreateRequest
import org.example.data.mapper.remote.toDomainModel
import org.example.data.mapper.remote.toUpdateRequest
import org.example.data.remote.dto.response.RouteResponseDto
import org.example.domain.model.Route
import org.example.domain.model.exception.RouteNotFoundException
import org.example.domain.repository.RouteRepository
import org.example.data.datasource.local.model.RouteLocalData
import org.example.data.mapper.sync.toRaw

class RouteRepositoryImpl(
    private val localDataSource: RouteDataSource,
    private val remoteDataSource: RouteRemoteDataSource,
) : BaseRepository(), RouteRepository {

    private val dataExceptionMapper = DataExceptionMapper()
    private val routes = mutableListOf<Route>()
    private var isLoaded = false


    override suspend fun getAllRoutes(): Result<List<Route>> {
        return runCatching {
            if (isLoaded) {
                return@runCatching routes.toList()
            }

            val loadedRoutes = loadLocalRoutes()

            routes.addAll(loadedRoutes)
            isLoaded = true

            routes.toList()
        }.mapFailureToDomain(dataExceptionMapper)
    }

    private fun loadLocalRoutes(): List<Route> {
        return localDataSource
            .getRoutes()
            .mapNotNull { result ->
                result.rawData?.let { data ->
                    mapLocalRoute(data)
                }
            }
    }



    override suspend fun getById(
        routeId: String
    ): Result<Route> {
        return getAllRoutes()
            .mapCatching { routes ->
                routes.firstOrNull {
                    it.id == routeId
                } ?: throw RouteNotFoundException()
            }
            .mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun save(
        route: Route
    ): Result<Route> {
        return runCatching {
            val request = route.toCreateRequest()
            val dto = remoteDataSource.save(request)
            val savedRoute = mapRemoteRoute(dto)
                    ?: throw NullRequiredFieldException("Route '${route.id}' save mapping failed.")
            routes.add(savedRoute)
            refreshLocalSnapshot()
            savedRoute
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun update(
        route: Route
    ): Result<Route> {
        return runCatching {
            val request = route.toUpdateRequest()
            val dto = remoteDataSource.update(id = route.id, request = request)
            val updatedRoute = mapRemoteRoute(dto)
                ?: throw NullRequiredFieldException("Route '${route.id}' update mapping failed.")

            routes.removeIf {
                it.id == route.id
            }
            routes.add(updatedRoute)
            refreshLocalSnapshot()
            updatedRoute
        }.mapFailureToDomain(dataExceptionMapper)
    }


    override suspend fun delete(
        id: String
    ): Result<String> {
        return runCatching {
            val deletedId = remoteDataSource.delete(id)
            routes.removeIf {
                it.id == deletedId
            }
            refreshLocalSnapshot()
            deletedId
        }.mapFailureToDomain(dataExceptionMapper)
    }
    private fun mapRemoteRoute(
        dto: RouteResponseDto
    ): Route? {
        return mapSafely(dto.routeId) {
            dto.toDomainModel()
        }
    }

    private fun mapLocalRoute(
        data: RouteLocalData
    ): Route? {
        return mapSafely(data.routeRaw.id) {
            data.toDomainModel()
        }
    }

    private suspend fun refreshLocalSnapshot() {
        val rawRoutes = remoteDataSource
            .getAll()
            .map { dto ->
                dto.toRaw()
            }

        localDataSource.replaceAll(rawRoutes)
    }
}

package org.example.data.repositoryImplementation

import org.example.data.repositoryImplementation.dependencies.RouteRepositoryDependencies
import org.example.domain.model.Route
import org.example.domain.repository.RouteRepository

class RouteRepositoryImpl(
    private val dependencies: RouteRepositoryDependencies
) : RouteRepository {


    @Suppress("TooGenericExceptionCaught")
    override fun getAllRoutes(): Result<List<Route>> {
        return runCatching {
            val rawResults = dependencies.localDataSource.getRoutes()
            val warnings = rawResults
                    .mapNotNull { it.errorMessage }
                    .toMutableList()
            val rawRoutes = rawResults.mapNotNull { it.rawData }
            mapRoutes(
                rawRoutes = rawRoutes,
                warnings = warnings
            )
        }
    }


    private fun mapRoutes(
        rawRoutes: List<org.example.data.dataholder.RouteRaw>,
        warnings: MutableList<String>
    ): List<Route> {

        return rawRoutes.mapNotNull { raw ->
            val origin =
                dependencies.warehouseMap[
                    normalizeId(raw.originHubId)
                ]


            val destination =
                dependencies.warehouseMap[
                    normalizeId(raw.destinationHubId)
                ]


            val validation =
                dependencies.validator.validate(
                    raw,
                    origin,
                    destination
                )


            if (validation.isNotEmpty()) {

                warnings.addAll(validation)

                null

            } else {

                dependencies.localMapper.map(
                    raw,
                    origin!!,
                    destination!!
                )
            }
        }
    }


    private fun normalizeId(
        id: String
    ): String =
        id.trim().uppercase()


    override suspend fun getById(
        routeId: String
    ): Route? {
        val responseDto = dependencies.remoteDataSource.getById(routeId)
        if (responseDto != null) {
            val originWarehouse =
                dependencies.warehouseRepository
                    .getById(responseDto.originHubId)
            val destinationWarehouse =
                dependencies.warehouseRepository
                    .getById(responseDto.destinationHubId)
            if (originWarehouse != null && destinationWarehouse != null
            ) {
                return dependencies.remoteMapper.mapToDomain(
                    raw = responseDto,
                    originWarehouse = originWarehouse,
                    destinationWarehouse = destinationWarehouse
                )
            }
        }
        return getAllRoutes()
            .getOrThrow().firstOrNull {
                it.id == routeId
            }
    }


    override suspend fun save(
        route: Route
    ): Route {
        val request = dependencies.remoteMapper.mapToCreateRequest(route)
        val responseDto = dependencies.remoteDataSource.save(request)
        val originWarehouse =
            dependencies.warehouseRepository
                .getById(responseDto.originHubId)
        val destinationWarehouse =
            dependencies.warehouseRepository
                .getById(responseDto.destinationHubId)
        if (
            originWarehouse == null || destinationWarehouse == null) {
            return route
        }
        return dependencies.remoteMapper
            .mapToDomain(
                raw = responseDto,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
    }


    override suspend fun update(
        route: Route
    ): Route {
        val request =
            dependencies.remoteMapper
                .mapToUpdateRequest(route)
        val responseDto =
            dependencies.remoteDataSource.update(id = route.id, request = request)
        val originWarehouse =
            dependencies.warehouseRepository
                .getById(responseDto.originHubId)
        val destinationWarehouse =
            dependencies.warehouseRepository.getById(responseDto.destinationHubId)
        if (originWarehouse == null || destinationWarehouse == null) {
            return route
        }
        return dependencies.remoteMapper
            .mapToDomain(
                raw = responseDto,
                originWarehouse = originWarehouse,
                destinationWarehouse = destinationWarehouse
            )
    }


    override suspend fun delete(
        id: String
    ): Boolean {

        return dependencies.remoteDataSource
            .delete(id)
    }
}

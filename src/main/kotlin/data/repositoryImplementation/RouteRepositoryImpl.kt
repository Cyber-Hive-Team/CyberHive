package org.example.data.repository

import org.example.data.datasource.RouteDataSource
import org.example.data.datasource.remote.RouteRemoteDatasource
import org.example.data.mapper.csv.RouteMapper
import org.example.data.mapper.remote.RouteDtoMapper
import org.example.data.validation.RouteValidator
import org.example.domain.model.Route
import org.example.domain.model.Warehouse
import org.example.domain.model.result.Result
import org.example.domain.repository.RouteRepository
import org.example.domain.repository.WarehouseRepository

class RouteRepositoryImpl(
    private val csvDataSource: RouteDataSource,
    private val mapper: RouteMapper,
    private val warehouseMap: Map<String, Warehouse>,
    private val validator: RouteValidator,
    private val remoteDataSource: RouteRemoteDatasource,
    private val remoteMapper: RouteDtoMapper,
    private val warehouseRepository: WarehouseRepository

) : RouteRepository {

    @Suppress("TooGenericExceptionCaught")
    override fun getAllRoutes(): Result<List<Route>> {
        return try {
            val rawResults = csvDataSource.getRoutes()
        val warnings = rawResults.mapNotNull { it.errorMessage }.toMutableList()
        val rawRoutes = rawResults.mapNotNull { it.rawData }
        val routes = mapRoutes(rawRoutes = rawRoutes, warnings = warnings)
        return Result(
            data = routes,
            errorMessage = warnings.takeIf { it.isNotEmpty() }
                ?.joinToString("; ")
        )
        } catch (e: Exception) {
            Result(data = emptyList(), errorMessage = "Failed to load routes: ${e.message}")
        }

    }

    private fun mapRoutes(
        rawRoutes: List<org.example.data.dataholder.RouteRaw>,
        warnings: MutableList<String>
    ): List<Route> =
        rawRoutes.mapNotNull { raw ->
            val origin = warehouseMap[normalizeId(raw.originHubId)]
            val destination = warehouseMap[normalizeId(raw.destinationHubId)]

            val validation = validator.validate(raw, origin, destination)
            if (validation.isNotEmpty()) {
                warnings.addAll(validation)
                null
            } else {
                mapper.map(raw, origin!!, destination!!)
            }

        }


    private fun normalizeId(id: String): String =
        id.trim().uppercase()
    override suspend fun getRemoteById(routeId: String): Route? {

        val responseDto =
            remoteDataSource.getById(routeId)
                ?: return null

        val originWarehouse =
            responseDto.originHubId
                .let { warehouseRepository.getWarehouseById(it) }
                ?: return null

        val destinationWarehouse =
            responseDto.destinationHubId
                .let { warehouseRepository.getWarehouseById(it) }
                ?: return null

        return remoteMapper.mapToDomain(
            raw = responseDto,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

    }

    override suspend fun save(route: Route): Route {
        val request = remoteMapper.mapToCreateRequest(route)
        val responseDto = remoteDataSource.save(request)
        val originWarehouse = responseDto.originHubId
            .let { warehouseRepository.getWarehouseById(it) }
        val destinationWarehouse = responseDto.destinationHubId
            .let { warehouseRepository.getWarehouseById(it) }
        if (originWarehouse == null || destinationWarehouse == null) {
            return route
        }

        return remoteMapper.mapToDomain(
            raw = responseDto,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

    }

    override suspend fun update(route: Route): Route {
        val request = remoteMapper.mapToUpdateRequest(route)
        val responseDto = remoteDataSource.update(id = route.id, request = request)
        val originWarehouse = responseDto.originHubId
            .let { warehouseRepository.getWarehouseById(it) }

        val destinationWarehouse = responseDto.destinationHubId
            .let { warehouseRepository.getWarehouseById(it) }

        if (originWarehouse == null || destinationWarehouse == null) {
            return route
        }

        return remoteMapper.mapToDomain(
            raw = responseDto,
            originWarehouse = originWarehouse,
            destinationWarehouse = destinationWarehouse
        )

    }

    override suspend fun delete(id: String): Boolean {
        return remoteDataSource.delete(id)

    }


}





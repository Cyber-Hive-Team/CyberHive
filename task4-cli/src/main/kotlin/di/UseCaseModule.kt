package org.example.di

import org.example.domain.algorithm.greedy.GreedyFleetDispatcher
import org.example.domain.algorithm.search.BreadthFirstSearchRouter
import org.example.domain.algorithm.search.Router
import org.example.domain.algorithm.search.WarehouseGraph
import org.example.domain.model.WarehouseNode
import org.example.domain.pricing.EcoStrategy
import org.example.domain.pricing.RoutePricingEngine
import org.example.domain.usecase.AddVehicleToHubUseCase
import org.example.domain.usecase.AnalyzeTreePerformanceUseCase
import org.example.domain.usecase.AssignPackageToCargoQueueUseCase
import org.example.domain.usecase.CalculatePricingPackageUseCase
import org.example.domain.usecase.DispatchFleetGreedyUseCase
import org.example.domain.usecase.DispatchVehicleUseCase
import org.example.domain.usecase.FindFewestHopsRouteUseCase
import org.example.domain.usecase.FindFleetShortageUseCase
import org.example.domain.usecase.FindFleetSurplusUseCase
import org.example.domain.usecase.FindLatePackagesUseCase
import org.example.domain.usecase.FindNearestWarehousesByRouteDistanceUseCase
import org.example.domain.usecase.FindOptimalPathUseCase
import org.example.domain.usecase.FindPackagesAtRiskOfDamageUseCase
import org.example.domain.usecase.FindPackagesByPriorityUseCase
import org.example.domain.usecase.FindPackagesWaitingTooLongInWarehouseUseCase
import org.example.domain.usecase.FindStationedVehiclesByCapacityUseCase
import org.example.domain.usecase.GetWarehouseLoadFactorUseCase
import org.example.domain.usecase.MarkWarehouseOutOfServiceUseCase
import org.example.domain.usecase.RedistributeFleetUseCase
import org.example.domain.usecase.ReroutePackageUseCase
import org.example.domain.usecase.TraceHubLineageUseCase
import org.example.domain.usecase.TrackVehicleCurrentLocationUseCase
import org.example.domain.usecase.crud.packages.CreatePackageUseCase
import org.example.domain.usecase.crud.packages.DeletePackageUseCase
import org.example.domain.usecase.crud.packages.GetPackageUseCase
import org.example.domain.usecase.crud.packages.UpdatePackageUseCase
import org.example.domain.usecase.crud.route.CreateRouteUseCase
import org.example.domain.usecase.crud.route.DeleteRouteUseCase
import org.example.domain.usecase.crud.route.GetRouteByIdUseCase
import org.example.domain.usecase.crud.route.UpdateRouteUseCase
import org.example.domain.usecase.crud.vehicle.CreateVehicleUseCase
import org.example.domain.usecase.crud.vehicle.DeleteVehicleUseCase
import org.example.domain.usecase.crud.vehicle.GetVehicleByIdUseCase
import org.example.domain.usecase.crud.vehicle.UpdateVehicleUseCase
import org.example.domain.usecase.crud.warehouse.CreateWarehouseUseCase
import org.example.domain.usecase.crud.warehouse.DeleteWarehouseUseCase
import org.example.domain.usecase.crud.warehouse.GetWarehouseByIdUseCase
import org.example.domain.usecase.crud.warehouse.UpdateWarehouseUseCase
import org.example.domain.validator.PackageValidator
import org.example.domain.validator.RouteValidator
import org.example.domain.validator.VehicleValidator
import org.example.domain.validator.WarehouseValidator
import org.koin.dsl.module

val useCaseModule = module {

    // Package CRUD
    factory {
        CreatePackageUseCase(
            packageRepository = get(),
            packageValidator = get<PackageValidator>()
        )
    }

    factory {
        DeletePackageUseCase(
            packageRepository = get()
        )
    }

    factory {
        GetPackageUseCase(
            packageRepository = get()
        )
    }

    factory {
        UpdatePackageUseCase(
            packageRepository = get(),
            packageValidator = get<PackageValidator>()
        )
    }


    // Route CRUD
    factory {
        CreateRouteUseCase(
            routeRepository = get(),
            routeValidator = get<RouteValidator>()
        )
    }

    factory {
        DeleteRouteUseCase(
            routeRepository = get()
        )
    }

    factory {
        GetRouteByIdUseCase(
            routeRepository = get()
        )
    }

    factory {
        UpdateRouteUseCase(
            routeRepository = get(),
            routeValidator = get<RouteValidator>()
        )
    }


    // Vehicle CRUD
    factory {
        CreateVehicleUseCase(
            vehicleRepository = get(),
            validator = get<VehicleValidator>()
        )
    }

    factory {
        DeleteVehicleUseCase(
            vehicleRepository = get()
        )
    }

    factory {
        GetVehicleByIdUseCase(
            vehicleRepository = get()
        )
    }

    factory {
        UpdateVehicleUseCase(
            vehicleRepository = get(),
            validator = get<VehicleValidator>()
        )
    }


    // Warehouse CRUD
    factory {
        CreateWarehouseUseCase(
            warehouseRepository = get(),
            warehouseValidator = get<WarehouseValidator>()
        )
    }

    factory {
        DeleteWarehouseUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        GetWarehouseByIdUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        UpdateWarehouseUseCase(
            warehouseRepository = get(),
            warehouseValidator = get<WarehouseValidator>()
        )
    }
    // Supporting dependencies

    factory {
        GreedyFleetDispatcher()
    }

    factory {
        RoutePricingEngine(
            strategy = EcoStrategy()
        )
    }


    // Other Use Cases

    factory {
        AddVehicleToHubUseCase(
            vehicleRepository = get(),
            warehouseRepository = get()
        )
    }

    factory {
        AnalyzeTreePerformanceUseCase()
    }

    factory {
        AssignPackageToCargoQueueUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        CalculatePricingPackageUseCase(
            packageRepository = get(),
            routeRepository = get(),
            pricingEngine = get()
        )
    }

    factory {
        DispatchFleetGreedyUseCase(
            vehicleRepository = get(),
            dispatcher = get()
        )
    }

    factory {
        DispatchVehicleUseCase(
            vehicleRepository = get(),
            packageRepository = get()
        )
    }

    factory {
        FindFleetShortageUseCase(
            warehouseRepository = get(),
            packageRepository = get(),
            vehicleRepository = get()
        )
    }

    factory {
        FindFleetSurplusUseCase(
            warehouseRepository = get(),
            packageRepository = get(),
            vehicleRepository = get()
        )
    }

    factory {
        FindLatePackagesUseCase(
            packageRepository = get()
        )
    }

    factory {
        FindOptimalPathUseCase(
            warehouseRepository = get(),
            routeRepository = get()
        )
    }

    factory {
        FindPackagesAtRiskOfDamageUseCase(
            packageRepository = get(),
            warehouseRepository = get()
        )
    }

    factory {
        FindPackagesByPriorityUseCase(
            packageRepository = get()
        )
    }

    factory {
        FindPackagesWaitingTooLongInWarehouseUseCase(
            packageRepository = get()
        )
    }

    factory {
        FindStationedVehiclesByCapacityUseCase(
            vehicleRepository = get()
        )
    }

    factory {
        GetWarehouseLoadFactorUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        MarkWarehouseOutOfServiceUseCase(
            warehouseRepository = get()
        )
    }

    factory {
        RedistributeFleetUseCase(
            findFleetShortageUseCase = get(),
            findFleetSurplusUseCase = get(),
            vehicleRepository = get()
        )
    }

    factory {
        TrackVehicleCurrentLocationUseCase(
            vehicleRepository = get()
        )
    }
    // Use Cases with runtime dependencies

    factory { (graph: WarehouseGraph) ->
        FindFewestHopsRouteUseCase(
            warehouseRepository = get(),
            router = BreadthFirstSearchRouter(graph)
        )
    }

    factory { (router: Router) ->
        FindNearestWarehousesByRouteDistanceUseCase(
            warehouseRepository = get(),
            router = router
        )
    }

    factory { (router: Router) ->
        ReroutePackageUseCase(
            packageRepository = get(),
            warehouseRepository = get(),
            router = router,
            pricingEngine = get()
        )
    }

    factory { (tree: WarehouseNode) ->
        TraceHubLineageUseCase(
            tree = tree
        )
    }
}

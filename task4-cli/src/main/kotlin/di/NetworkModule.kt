package org.example.di


import io.ktor.client.HttpClient
import org.example.data.datasource.remote.PackageRemoteDataSource
import org.example.data.datasource.remote.RouteRemoteDataSource
import org.example.data.datasource.remote.VehicleRemoteDataSource
import org.example.data.datasource.remote.WarehouseRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabasePackageRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabaseRouteRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabaseVehicleRemoteDataSource
import org.example.data.datasource.remote.supabase.SupabaseWarehouseRemoteDataSource
import org.example.data.remote.client.SupabaseHttpClient
import org.example.data.remote.config.SupabaseConfig
import org.example.data.retry.RetryWithBackoff
import org.koin.dsl.module

val networkModule = module {

    single {
        SupabaseConfig(
            url = requireNotNull(
                System.getenv("SUPABASE_URL")
            ),
            publishableKey = requireNotNull(
                System.getenv("SUPABASE_PUBLISHABLE_KEY")
            )
        )
    }

    single {
        SupabaseHttpClient(
            config = get()
        )
    }

    single<HttpClient> {
        get<SupabaseHttpClient>().create()
    }

    single<PackageRemoteDataSource> {
        SupabasePackageRemoteDataSource(
            client = get(),
            baseUrl = "${get<SupabaseConfig>().url}/rest/v1",
            retry = get()

        )
    }

    single<RouteRemoteDataSource> {
        SupabaseRouteRemoteDataSource(
            client = get(),
            baseUrl = "${get<SupabaseConfig>().url}/rest/v1",
            retry = get()
        )
    }

    single<VehicleRemoteDataSource> {
        SupabaseVehicleRemoteDataSource(
            client = get(),
            baseUrl = "${get<SupabaseConfig>().url}/rest/v1",
            retry = get()
        )
    }

    single<WarehouseRemoteDataSource> {
        SupabaseWarehouseRemoteDataSource(
            client = get(),
            baseUrl = "${get<SupabaseConfig>().url}/rest/v1",
            retry = get()
        )
    }
    single {
        RetryWithBackoff()
    }
}

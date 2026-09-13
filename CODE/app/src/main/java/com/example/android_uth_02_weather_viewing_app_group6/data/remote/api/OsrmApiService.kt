package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OsrmRouteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
interface OsrmApiService {

    @GET("route/v1/driving/{coordinates}")
    suspend fun getDrivingRoute(
        @Path("coordinates", encoded = true) coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson",
        @Query("steps") steps: Boolean = false
    ): Response<OsrmRouteResponse>
}

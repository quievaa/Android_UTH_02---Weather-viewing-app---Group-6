package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApiService {

    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") name: String,
        @Query("count") count: Int = 1,
        @Query("language") language: String = "vi",
        @Query("format") format: String = "json",
    ): Response<GeocodingResponse>
}

package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenMeteoWeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,pressure_msl",
        @Query("daily") daily: String = "temperature_2m_min,temperature_2m_max",
        @Query("forecast_days") forecastDays: Int = 1,
        @Query("timezone") timezone: String = "auto",
    ): Response<OpenMeteoWeatherResponse>
}

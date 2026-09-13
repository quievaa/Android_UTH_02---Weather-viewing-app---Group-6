package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenMeteoWeatherResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenWeatherMapResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.WeatherApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // 1. Open-Meteo
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

interface OpenWeatherMapApiService {

    // 2. OpenWeatherMap
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi",
    ): Response<OpenWeatherMapResponse>

    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi",
    ): Response<OpenWeatherMapResponse>
}

interface WeatherApiDotComService {

    // 3. WeatherAPI.com
    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("lang") lang: String = "vi",
    ): Response<WeatherApiResponse>
}

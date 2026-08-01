package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

import com.example.android_uth_02_weather_viewing_app_group6.BuildConfig
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.ForecastResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<WeatherResponse>

    @GET("forecast")
    suspend fun getForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): Response<ForecastResponse>
}

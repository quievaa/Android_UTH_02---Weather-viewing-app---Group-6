package com.example.android_uth_02_weather_viewing_app_group6.domain.model

import com.example.android_uth_02_weather_viewing_app_group6.ui.model.DailyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.HourlyForecast

data class CurrentWeather(
    val cityName: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val description: String,
    val weatherMain: String,
    val humidityPercent: Int,
    val pressureHpa: Double,
    val windSpeedMps: Double,
    val windDirectionDeg: Int?,
    val latitude: Double?,
    val longitude: Double?,
    val iconCode: String?,
    val apiProvider: String = "Open-Meteo",
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList(),
)

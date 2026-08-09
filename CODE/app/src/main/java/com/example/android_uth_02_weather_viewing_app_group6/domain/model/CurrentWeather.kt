package com.example.android_uth_02_weather_viewing_app_group6.domain.model

/**
 * Model nghiệp vụ dùng cho giao diện Current Weather.
 * Không phụ thuộc vào cấu trúc JSON của nhà cung cấp API.
 */
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
)

package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

// OpenWeatherMap Data Models
data class OpenWeatherMapResponse(
    @SerializedName("name") val name: String?,
    @SerializedName("coord") val coord: OpenWeatherCoordDto?,
    @SerializedName("weather") val weather: List<OpenWeatherWeatherDto>?,
    @SerializedName("main") val main: OpenWeatherMainDto?,
    @SerializedName("wind") val wind: OpenWeatherWindDto?,
    @SerializedName("dt") val dt: Long?,
)

data class OpenWeatherCoordDto(
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
)

data class OpenWeatherWeatherDto(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String,
)

data class OpenWeatherMainDto(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Double,
    @SerializedName("humidity") val humidity: Int,
)

data class OpenWeatherWindDto(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int?,
)

// WeatherAPI.com Data Models
data class WeatherApiResponse(
    @SerializedName("location") val location: WeatherApiLocationDto?,
    @SerializedName("current") val current: WeatherApiCurrentDto?,
)

data class WeatherApiLocationDto(
    @SerializedName("name") val name: String,
    @SerializedName("region") val region: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
)

data class WeatherApiCurrentDto(
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("feelslike_c") val feelsLikeC: Double,
    @SerializedName("condition") val condition: WeatherApiConditionDto?,
    @SerializedName("wind_kph") val windKph: Double,
    @SerializedName("wind_degree") val windDegree: Int?,
    @SerializedName("pressure_mb") val pressureMb: Double,
    @SerializedName("humidity") val humidity: Int,
)

data class WeatherApiConditionDto(
    @SerializedName("text") val text: String,
    @SerializedName("icon") val icon: String,
    @SerializedName("code") val code: Int,
)

// Wttr.in Keyless Global Weather DTO
data class WttrInResponse(
    @SerializedName("current_condition") val currentCondition: List<WttrInCurrentConditionDto>?,
    @SerializedName("nearest_area") val nearestArea: List<WttrInNearestAreaDto>?,
)

data class WttrInCurrentConditionDto(
    @SerializedName("temp_C") val tempC: String?,
    @SerializedName("FeelsLikeC") val feelsLikeC: String?,
    @SerializedName("humidity") val humidity: String?,
    @SerializedName("pressure") val pressure: String?,
    @SerializedName("windspeedKmph") val windspeedKmph: String?,
    @SerializedName("winddirDegree") val winddirDegree: String?,
    @SerializedName("weatherDesc") val weatherDesc: List<WttrInValueDto>?,
    @SerializedName("weatherCode") val weatherCode: String?,
)

data class WttrInNearestAreaDto(
    @SerializedName("areaName") val areaName: List<WttrInValueDto>?,
    @SerializedName("country") val country: List<WttrInValueDto>?,
    @SerializedName("latitude") val latitude: String?,
    @SerializedName("longitude") val longitude: String?,
)

data class WttrInValueDto(
    @SerializedName("value") val value: String?,
)

// API Health Check Result
data class ApiHealthStatus(
    val providerName: String,
    val isOnline: Boolean,
    val latencyMs: Long,
    val message: String,
    val isPrimary: Boolean = false,
)

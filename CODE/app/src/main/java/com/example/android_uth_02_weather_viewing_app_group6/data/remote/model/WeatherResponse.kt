package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoWeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("current") val current: CurrentDto,
    @SerializedName("current_units") val currentUnits: CurrentUnitsDto?,
    @SerializedName("daily") val daily: DailyDto?,
)

data class CurrentDto(
    @SerializedName("time") val time: String?,
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("apparent_temperature") val apparentTemperature: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("wind_direction_10m") val windDirection: Int?,
    @SerializedName("pressure_msl") val pressure: Double,
)

data class CurrentUnitsDto(
    @SerializedName("temperature_2m") val temperature: String?,
    @SerializedName("relative_humidity_2m") val humidity: String?,
    @SerializedName("wind_speed_10m") val windSpeed: String?,
    @SerializedName("pressure_msl") val pressure: String?,
)

data class DailyDto(
    @SerializedName("temperature_2m_min") val minTemperature: List<Double>?,
    @SerializedName("temperature_2m_max") val maxTemperature: List<Double>?,
)

package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoWeatherResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("timezone") val timezone: String?,
    @SerializedName("current") val current: CurrentDto,
    @SerializedName("current_units") val currentUnits: CurrentUnitsDto?,
    @SerializedName("hourly") val hourly: HourlyDto? = null,
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

data class HourlyDto(
    @SerializedName("time") val time: List<String>? = null,
    @SerializedName("temperature_2m") val temperature: List<Double>? = null,
    @SerializedName("precipitation_probability") val precipitationProbability: List<Int>? = null,
    @SerializedName("weather_code") val weatherCode: List<Int>? = null,
)

data class DailyDto(
    @SerializedName("time") val time: List<String>? = null,
    @SerializedName("temperature_2m_min") val minTemperature: List<Double>?,
    @SerializedName("temperature_2m_max") val maxTemperature: List<Double>?,
    @SerializedName("weather_code") val weatherCode: List<Int>? = null,
    @SerializedName("precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null,
    @SerializedName("precipitation_sum") val precipitationSum: List<Double>? = null,
    @SerializedName("wind_speed_10m_max") val windSpeedMax: List<Double>? = null,
    @SerializedName("uv_index_max") val uvIndexMax: List<Double>? = null,
)

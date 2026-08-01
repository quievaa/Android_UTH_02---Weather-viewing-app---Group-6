package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val cityName: String,
    @SerializedName("coord") val coord: CoordDto?,
    @SerializedName("weather") val weather: List<WeatherDto>,
    @SerializedName("main") val main: MainDto,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("dt") val dt: Long
)

data class CoordDto(
    @SerializedName("lon") val lon: Double,
    @SerializedName("lat") val lat: Double
)

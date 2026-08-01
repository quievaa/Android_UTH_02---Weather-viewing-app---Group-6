package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<ForecastItemDto>,
    @SerializedName("city") val city: CityDto
)

data class ForecastItemDto(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: MainDto,
    @SerializedName("weather") val weather: List<WeatherDto>,
    @SerializedName("wind") val wind: WindDto?,
    @SerializedName("dt_txt") val dtTxt: String
)

data class CityDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)

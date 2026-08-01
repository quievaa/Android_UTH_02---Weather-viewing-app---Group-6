package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

data class WeatherDto(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

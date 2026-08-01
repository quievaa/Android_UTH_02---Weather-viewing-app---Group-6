package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

data class WindDto(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int
)

package com.example.android_uth_02_weather_viewing_app_group6.ui.model

data class ForecastItem(
    val day: String,
    val condition: String,
    val temperature: String,
)

data class FavoriteCity(
    val name: String,
    val condition: String,
    val temperature: String,
)

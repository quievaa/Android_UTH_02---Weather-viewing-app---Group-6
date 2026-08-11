package com.example.android_uth_02_weather_viewing_app_group6.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherScreen(
    val title: String,
    val icon: ImageVector,
) {
    Home("Home", Icons.Default.Home),
    Search("Search", Icons.Default.Search),
    Forecast("Forecast", Icons.Default.Cloud),
    Favorite("Favorite", Icons.Default.Favorite),
    Settings("Settings", Icons.Default.Settings),
}

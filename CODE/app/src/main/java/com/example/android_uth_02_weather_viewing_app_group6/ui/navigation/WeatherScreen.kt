package com.example.android_uth_02_weather_viewing_app_group6.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class WeatherScreen(
    val title: String,
    val icon: ImageVector,
    val testTag: String
) {
    Home("Trang chủ", Icons.Outlined.Home, "nav_home"),
    Radar("Radar", Icons.Outlined.Layers, "nav_radar"),
    TripPlanner("Lộ trình", Icons.AutoMirrored.Outlined.AltRoute, "nav_trip_planner"),
    Forecast("Dự báo", Icons.Outlined.CalendarMonth, "nav_forecast"),
    Favorite("Yêu thích", Icons.Outlined.FavoriteBorder, "nav_favorite"),
    Search("Tìm kiếm", Icons.Outlined.Search, "nav_search"),
    Settings("Cài đặt", Icons.Outlined.Settings, "nav_settings");

    // Helper property to check if tab is on bottom bar
    val isBottomNavTab: Boolean
        get() = this != Search
}

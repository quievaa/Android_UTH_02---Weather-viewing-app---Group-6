package com.example.android_uth_02_weather_viewing_app_group6.data.model

    data class FavoriteCity(
        val id: String,          // Mã ID (ví dụ: "lat_lon")
        val cityName: String,    // Tên thành phố
        val country: String,     // Tên quốc gia
        val latitude: Double,    // Vĩ độ
        val longitude: Double    // Kinh độ
    )
}
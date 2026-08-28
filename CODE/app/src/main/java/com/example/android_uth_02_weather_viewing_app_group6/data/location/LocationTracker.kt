package com.example.android_uth_02_weather_viewing_app_group6.data.location

import android.location.Location

interface LocationTracker {
    suspend fun getCurrentLocation(): Location?
}

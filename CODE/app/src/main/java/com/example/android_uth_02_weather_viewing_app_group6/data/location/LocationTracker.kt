package com.example.android_uth_02_weather_viewing_app_group6.data.location

import android.location.Location

sealed interface LocationResult {
    data class Success(val location: Location) : LocationResult
    data class Error(val message: String) : LocationResult
    data object PermissionDenied : LocationResult
    data object GpsDisabled : LocationResult
}

interface LocationTracker {
    suspend fun getCurrentLocation(): LocationResult
}

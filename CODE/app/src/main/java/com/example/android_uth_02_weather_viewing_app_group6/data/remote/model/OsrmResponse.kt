package com.example.android_uth_02_weather_viewing_app_group6.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects cho OSRM (Open Source Routing Machine) API
 * Endpoint: https://router.project-osrm.org/route/v1/driving/{coordinates}
 */
data class OsrmRouteResponse(
    @SerializedName("code") val code: String?,
    @SerializedName("routes") val routes: List<OsrmRoute>?,
    @SerializedName("waypoints") val waypoints: List<OsrmWaypoint>?
)

data class OsrmRoute(
    @SerializedName("distance") val distance: Double, // Mét
    @SerializedName("duration") val duration: Double, // Giây
    @SerializedName("geometry") val geometry: OsrmGeometry?,
    @SerializedName("legs") val legs: List<OsrmLeg>?
)

data class OsrmGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>, // Mảng các điểm [lon, lat]
    @SerializedName("type") val type: String
)

data class OsrmLeg(
    @SerializedName("distance") val distance: Double,
    @SerializedName("duration") val duration: Double,
    @SerializedName("summary") val summary: String?
)

data class OsrmWaypoint(
    @SerializedName("name") val name: String?,
    @SerializedName("location") val location: List<Double>? // [lon, lat]
)

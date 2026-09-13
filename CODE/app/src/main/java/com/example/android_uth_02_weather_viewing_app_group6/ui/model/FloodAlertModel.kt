package com.example.android_uth_02_weather_viewing_app_group6.ui.model

/**
 * Cấp độ cảnh báo rủi ro ngập úng đô thị
 */
enum class FloodAlertLevel(val labelVi: String, val badgeColorHex: Long) {
    SAFE("An toàn", 0xFF10B981),        // Xanh ngọc
    CAUTION("Theo dõi", 0xFFFBBF24),     // Vàng
    WARNING("Cảnh báo", 0xFFF97316),     // Cam
    SEVERE("Nguy hiểm", 0xFFEF4444)      // Đỏ
}

/**
 * Dữ liệu một điểm đen / tuyến đường có nguy cơ ngập úng
 */
data class FloodBlackspot(
    val id: String,
    val streetName: String,
    val district: String,
    val city: String,
    val latitude: Double,
    val longitude: Double,
    val expectedDepthCm: String,
    val vehicleAdvice: String,
    val detourAdvice: String,
    val isNearUthCampus: Boolean = false,
    val uthCampusNote: String? = null
)

/**
 * Báo cáo tổng hợp cảnh báo rủi ro ngập úng đô thị
 */
data class UrbanFloodReport(
    val hasAlert: Boolean,
    val level: FloodAlertLevel,
    val title: String,
    val summary: String,
    val timeWindow: String,
    val cityName: String,
    val rainfallEstimateMm: Double,
    val isTideAffected: Boolean,
    val highRiskStreets: List<FloodBlackspot> = emptyList(),
    val safetyTips: List<String> = emptyList()
)

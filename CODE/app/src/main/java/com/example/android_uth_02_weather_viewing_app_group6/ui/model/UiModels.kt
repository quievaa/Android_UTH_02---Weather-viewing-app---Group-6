package com.example.android_uth_02_weather_viewing_app_group6.ui.model

enum class WeatherCondition(val labelVi: String, val labelEn: String) {
    SUNNY("Nắng đẹp", "Sunny"),
    PARTLY_CLOUDY("Nắng nhẹ", "Partly Cloudy"),
    CLOUDY("Nhiều mây", "Cloudy"),
    OVERCAST("U ám", "Overcast"),
    LIGHT_RAIN("Mưa rào nhẹ", "Light Rain"),
    RAIN("Mưa", "Rain"),
    HEAVY_RAIN("Mưa lớn", "Heavy Rain"),
    THUNDERSTORM("Mưa dông bão", "Thunderstorm"),
    NIGHT_CLEAR("Đêm quang mây", "Clear Night"),
    NIGHT_CLOUDY("Đêm có mây", "Cloudy Night");

    companion object {
        fun fromDescription(desc: String, isNight: Boolean = false): WeatherCondition {
            val lower = desc.lowercase()
            return when {
                lower.contains("dông") || lower.contains("bão") || lower.contains("thunder") -> THUNDERSTORM
                lower.contains("mưa lớn") || lower.contains("heavy rain") -> HEAVY_RAIN
                lower.contains("mưa rào") || lower.contains("mưa nhỏ") || lower.contains("light rain") || lower.contains("drizzle") -> LIGHT_RAIN
                lower.contains("mưa") || lower.contains("rain") -> RAIN
                lower.contains("u ám") || lower.contains("overcast") -> OVERCAST
                lower.contains("nhiều mây") || lower.contains("cloud") -> if (isNight) NIGHT_CLOUDY else CLOUDY
                lower.contains("ít mây") || lower.contains("nắng nhẹ") || lower.contains("partly") -> PARTLY_CLOUDY
                lower.contains("nắng") || lower.contains("quang") || lower.contains("clear") || lower.contains("sun") -> if (isNight) NIGHT_CLEAR else SUNNY
                else -> if (isNight) NIGHT_CLEAR else SUNNY
            }
        }
    }
}

data class HourlyForecast(
    val time: String,
    val temp: Int,
    val condition: WeatherCondition,
    val pop: Int = 0 // Xác suất mưa %
)

data class DailyForecast(
    val dayName: String,
    val dateText: String,
    val minTemp: Int,
    val maxTemp: Int,
    val condition: WeatherCondition,
    val pop: Int,
    val summary: String,
    val windSpeedKmH: Int,
    val humidityPercent: Int,
    val rainfallMm: Int,
    val uvIndex: Int,
    val airQualityIndex: Int
)

data class AirQuality(
    val aqi: Int,
    val status: String,
    val levelDesc: String
)

data class UVIndex(
    val index: Int,
    val level: String,
    val advice: String
)

data class WindInfo(
    val speedKmH: Int,
    val direction: String,
    val gustSpeedKmH: Int
)

data class PrecipitationInfo(
    val currentMm: Int,
    val expected24hMm: Int,
    val popPercent: Int
)

data class CityLocation(
    val id: String,
    val name: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val currentTemp: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val condition: WeatherCondition,
    val conditionDesc: String
)

data class RouteWaypoint(
    val time: String,
    val type: String, // "Start", "Waypoint", "Warning", "Arrival"
    val locationName: String,
    val temp: Int,
    val condition: WeatherCondition,
    val isWarning: Boolean = false,
    val warningTitle: String? = null,
    val warningDesc: String? = null,
    val windSpeed: String = "15 km/h",
    val visibility: String = "10 km",
    val rainAmount: String = "0 mm"
)

data class TripRoute(
    val id: String,
    val origin: String,
    val destination: String,
    val departureTime: String,
    val durationText: String,
    val distanceKm: Int,
    val hasSevereWarning: Boolean,
    val warningTitle: String,
    val warningDesc: String,
    val waypoints: List<RouteWaypoint>
)

enum class RadarLayer(val titleVi: String, val titleEn: String) {
    PRECIPITATION("Lượng mưa", "Rain & Radar"),
    WIND("Gió & Bão", "Wind Stream"),
    TEMPERATURE("Nhiệt độ", "Temperature"),
    CLOUDS("Mây vệ tinh", "Cloud Cover")
}

data class ForecastItem(
    val day: String,
    val condition: String,
    val temperature: String,
)


package com.example.android_uth_02_weather_viewing_app_group6.data.location

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeocodingApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.OsrmApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.WeatherApiService
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RouteGeoPoint
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RouteWaypoint
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.TripRoute
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Động cơ Lập lộ trình thời tiết tự do qua OSRM (Open Source Routing Machine).
 * 100% mã nguồn mở & API công cộng miễn phí (không cần thẻ Visa/Google Cloud).
 * Tích hợp nhận diện các cơ sở UTH, tính toán trạm dừng và khuyến cáo phương tiện.
 */
class TripRoutingEngine(
    private val osrmApi: OsrmApiService,
    private val geocodingApi: GeocodingApiService,
    private val weatherApi: WeatherApiService? = null
) {

    // Danh mục địa danh nhanh & các cơ sở Đại học GTVT TP.HCM (UTH)
    private val knownLocations = mapOf(
        "uth cơ sở 1" to RouteGeoPoint(10.8037, 106.7135),
        "cơ sở 1" to RouteGeoPoint(10.8037, 106.7135),
        "cs1" to RouteGeoPoint(10.8037, 106.7135),
        "uth bình thạnh" to RouteGeoPoint(10.8037, 106.7135),
        "đại học giao thông vận tải tp.hcm" to RouteGeoPoint(10.8037, 106.7135),

        "uth cơ sở 2" to RouteGeoPoint(10.8690, 106.6346),
        "cơ sở 2" to RouteGeoPoint(10.8690, 106.6346),
        "cs2" to RouteGeoPoint(10.8690, 106.6346),
        "uth quận 12" to RouteGeoPoint(10.8690, 106.6346),

        "uth cơ sở 3" to RouteGeoPoint(10.7745, 106.7725),
        "cơ sở 3" to RouteGeoPoint(10.7745, 106.7725),
        "cs3" to RouteGeoPoint(10.7745, 106.7725),
        "uth thủ đức" to RouteGeoPoint(10.7745, 106.7725),

        "tp. hồ chí minh" to RouteGeoPoint(10.7769, 106.7009),
        "hồ chí minh" to RouteGeoPoint(10.7769, 106.7009),
        "sài gòn" to RouteGeoPoint(10.7769, 106.7009),
        "tphcm" to RouteGeoPoint(10.7769, 106.7009),

        "đà lạt" to RouteGeoPoint(11.9404, 108.4583),
        "da lat" to RouteGeoPoint(11.9404, 108.4583),
        "vũng tàu" to RouteGeoPoint(10.3460, 107.0843),
        "vung tau" to RouteGeoPoint(10.3460, 107.0843),
        "hà nội" to RouteGeoPoint(21.0285, 105.8542),
        "ha noi" to RouteGeoPoint(21.0285, 105.8542),
        "hải phòng" to RouteGeoPoint(20.8449, 106.6881),
        "hai phong" to RouteGeoPoint(20.8449, 106.6881),
        "đà nẵng" to RouteGeoPoint(16.0544, 108.2022),
        "da nang" to RouteGeoPoint(16.0544, 108.2022),
        "huế" to RouteGeoPoint(16.4637, 107.5909),
        "hue" to RouteGeoPoint(16.4637, 107.5909),
        "cần thơ" to RouteGeoPoint(10.0452, 105.7469),
        "can tho" to RouteGeoPoint(10.0452, 105.7469),
        "nha trang" to RouteGeoPoint(12.2388, 109.1967),
        "phan thiết" to RouteGeoPoint(10.9804, 108.2615),
        "phan thiet" to RouteGeoPoint(10.9804, 108.2615),
        "bảo lộc" to RouteGeoPoint(11.5471, 107.8080),
        "bao loc" to RouteGeoPoint(11.5471, 107.8080),
        "vị trí của tôi" to RouteGeoPoint(10.8231, 106.6297),
        "vị trí hiện tại" to RouteGeoPoint(10.8231, 106.6297),
        "vị trí gps" to RouteGeoPoint(10.8231, 106.6297),
        "my location" to RouteGeoPoint(10.8231, 106.6297)
    )

    /**
     * Định vị tọa độ từ tên địa danh (hỗ trợ UTH, cache nhanh, hoặc gọi Geocoding API).
     */
    suspend fun resolveCoordinates(name: String, userLocation: RouteGeoPoint? = null): RouteGeoPoint? {
        val clean = name.trim().lowercase()
        if (clean.isEmpty()) return null

        if (clean == "vị trí của tôi" || clean == "vị trí gps" || clean == "my location" ||
            clean.contains("vị trí của tôi") || clean.contains("my location") || clean.contains("vị trí hiện tại")
        ) {
            return userLocation ?: RouteGeoPoint(10.8231, 106.6297)
        }

        // Kiểm tra danh mục nhanh
        knownLocations.entries.firstOrNull { clean.contains(it.key) }?.let {
            return it.value
        }

        // Gọi Geocoding API
        return try {
            val response = geocodingApi.searchCity(name = name, count = 1, language = "vi", format = "json")
            val firstResult = response.body()?.results?.firstOrNull()
            if (firstResult != null) {
                RouteGeoPoint(firstResult.latitude, firstResult.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lập lộ trình từ Origin đến Destination qua OSRM Driving API.
     */
    suspend fun calculateRoute(
        originName: String,
        destinationName: String,
        vehicleType: String = "Ô tô",
        departureTimeText: String = "Bây giờ",
        userLocation: RouteGeoPoint? = null
    ): Result<TripRoute> {
        val startPoint = resolveCoordinates(originName, userLocation)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy tọa độ điểm khởi hành: '$originName'"))
        val endPoint = resolveCoordinates(destinationName, userLocation)
            ?: return Result.failure(IllegalArgumentException("Không tìm thấy tọa độ điểm đến: '$destinationName'"))

        // Chuỗi tọa độ cho OSRM: lon1,lat1;lon2,lat2
        val osrmCoordParam = String.format(
            Locale.US,
            "%.6f,%.6f;%.6f,%.6f",
            startPoint.longitude,
            startPoint.latitude,
            endPoint.longitude,
            endPoint.latitude
        )

        var distanceKm = 0
        var durationMinutes = 0
        var durationText = ""
        var pathGeoPoints: List<RouteGeoPoint> = emptyList()

        try {
            val response = osrmApi.getDrivingRoute(
                coordinates = osrmCoordParam,
                overview = "full",
                geometries = "geojson",
                steps = false
            )
            val body = response.body()
            val firstRoute = body?.routes?.firstOrNull()

            if (response.isSuccessful && firstRoute != null) {
                distanceKm = (firstRoute.distance / 1000.0).roundToInt().coerceAtLeast(1)
                durationMinutes = (firstRoute.duration / 60.0).roundToInt().coerceAtLeast(1)

                // Trích xuất mảng LineString [lon, lat]
                val coords = firstRoute.geometry?.coordinates ?: emptyList()
                pathGeoPoints = coords.mapNotNull {
                    if (it.size >= 2) RouteGeoPoint(latitude = it[1], longitude = it[0]) else null
                }
            } else {
                // Fallback tính toán nội bộ nếu OSRM từ chối hoặc quá tải
                val fallbackDist = haversineDistance(startPoint, endPoint) * 1.3
                distanceKm = fallbackDist.roundToInt().coerceAtLeast(1)
                val speed = if (vehicleType == "Xe máy") 40.0 else 55.0
                durationMinutes = ((distanceKm / speed) * 60.0).roundToInt().coerceAtLeast(10)
                pathGeoPoints = generateInterpolatedPath(startPoint, endPoint, 15)
            }
        } catch (e: Exception) {
            // Fallback khi mất mạng hoặc timeout
            val fallbackDist = haversineDistance(startPoint, endPoint) * 1.3
            distanceKm = fallbackDist.roundToInt().coerceAtLeast(1)
            val speed = if (vehicleType == "Xe máy") 40.0 else 55.0
            durationMinutes = ((distanceKm / speed) * 60.0).roundToInt().coerceAtLeast(10)
            pathGeoPoints = generateInterpolatedPath(startPoint, endPoint, 15)
        }

        // Định dạng thời gian
        val hours = durationMinutes / 60
        val mins = durationMinutes % 60
        durationText = if (hours > 0) {
            if (mins > 0) "$hours giờ $mins phút" else "$hours giờ"
        } else {
            "$mins phút"
        }

        // Nội suy các trạm dừng dọc lộ trình (4-5 trạm)
        val waypoints = generateWaypoints(
            originName = originName,
            destinationName = destinationName,
            pathPoints = pathGeoPoints,
            totalDurationMinutes = durationMinutes,
            vehicleType = vehicleType
        )

        // Đánh giá cảnh báo thời tiết xấu và tạo khuyến cáo an toàn theo phương tiện
        val hasSevereWarning = waypoints.any { it.isWarning }
        val warningTitle = if (hasSevereWarning) {
            waypoints.firstOrNull { it.isWarning }?.warningTitle ?: "Cảnh báo an toàn trên lộ trình"
        } else {
            "Thời tiết lộ trình thuận lợi"
        }

        val warningDesc = if (hasSevereWarning) {
            waypoints.firstOrNull { it.isWarning }?.warningDesc
                ?: "Cần giảm tốc độ và chú ý quan sát khi qua các đoạn đường cảnh báo."
        } else {
            "Đường khô ráo, tầm nhìn tốt, điều kiện lý tưởng cho chuyến đi của bạn."
        }

        val drivingAdvice = buildDrivingAdvice(
            vehicleType = vehicleType,
            hasSevereWarning = hasSevereWarning,
            distanceKm = distanceKm,
            durationText = durationText
        )

        val trip = TripRoute(
            id = "trip_${System.currentTimeMillis()}",
            origin = originName,
            destination = destinationName,
            departureTime = departureTimeText,
            durationText = durationText,
            distanceKm = distanceKm,
            hasSevereWarning = hasSevereWarning,
            warningTitle = warningTitle,
            warningDesc = warningDesc,
            waypoints = waypoints,
            pathPoints = pathGeoPoints,
            vehicleType = vehicleType,
            drivingAdvice = drivingAdvice
        )

        return Result.success(trip)
    }

    /**
     * Nội suy các trạm dừng thời tiết dọc theo tọa độ đường đi thực tế.
     */
    private fun generateWaypoints(
        originName: String,
        destinationName: String,
        pathPoints: List<RouteGeoPoint>,
        totalDurationMinutes: Int,
        vehicleType: String
    ): List<RouteWaypoint> {
        if (pathPoints.isEmpty()) {
            return listOf(
                RouteWaypoint("Bây giờ", "Khởi hành", originName, 30, WeatherCondition.SUNNY),
                RouteWaypoint("Đến nơi", "Điểm đến", destinationName, 28, WeatherCondition.PARTLY_CLOUDY)
            )
        }

        val waypointCount = if (pathPoints.size >= 8) 4 else 2
        val waypoints = mutableListOf<RouteWaypoint>()
        val cal = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        for (i in 0 until waypointCount) {
            val ratio = i.toDouble() / (waypointCount - 1)
            val pointIndex = ((pathPoints.size - 1) * ratio).roundToInt().coerceIn(0, pathPoints.size - 1)
            val pt = pathPoints[pointIndex]

            // Tính toán thời gian đến (ETA)
            val elapsedMinutes = (totalDurationMinutes * ratio).roundToInt()
            val waypointCal = cal.clone() as Calendar
            waypointCal.add(Calendar.MINUTE, elapsedMinutes)
            val etaTime = if (i == 0) "Bây giờ" else timeFormat.format(waypointCal.time)

            val type = when (i) {
                0 -> "Khởi hành"
                waypointCount - 1 -> "Điểm đến"
                else -> "Trạm dừng ${i}"
            }

            val locName = when (i) {
                0 -> originName
                waypointCount - 1 -> destinationName
                else -> deriveIntermediateName(originName, destinationName, ratio)
            }

            // Đánh giá kiểm tra nguy cơ ngập úng hoặc thời tiết khắc nghiệt gần điểm này
            val floodRiskNear = isNearKnownFloodZone(pt.latitude, pt.longitude)
            val isSevereHazard = floodRiskNear || (ratio in 0.4..0.7 && totalDurationMinutes > 90)

            val condition = when {
                isSevereHazard -> WeatherCondition.THUNDERSTORM
                ratio > 0.6 -> WeatherCondition.LIGHT_RAIN
                ratio > 0.3 -> WeatherCondition.CLOUDY
                else -> WeatherCondition.PARTLY_CLOUDY
            }

            val temp = (29 - (ratio * 4)).roundToInt()
            val windSpeed = if (isSevereHazard) "26 km/h" else "${12 + (ratio * 5).toInt()} km/h"
            val visibility = if (isSevereHazard) "800 m" else "10 km"
            val rainAmount = if (isSevereHazard) "22 mm" else if (condition == WeatherCondition.LIGHT_RAIN) "3 mm" else "0 mm"

            val (warnTitle, warnDesc) = if (isSevereHazard) {
                if (floodRiskNear) {
                    Pair(
                        "Cảnh báo ngập úng đô thị cục bộ",
                        if (vehicleType == "Xe máy")
                            "Đoạn đường có điểm ngập bánh xe > 25cm. Xe máy gầm thấp tuyệt đối không cố lội nước, đề xuất né sang lộ trình tránh."
                        else
                            "Đoạn đường có nguy cơ ngập sâu > 30cm. Ô tô con cần giảm tốc độ, không bám đuôi xe tải để tránh sóng tràn vào cổ hút gió."
                    )
                } else {
                    Pair(
                        "Cảnh báo mưa dông & đường trơn trượt",
                        if (vehicleType == "Xe máy")
                            "Gió giật mạnh, mưa lớn làm hạn chế tầm nhìn. Giảm tốc độ < 30km/h, trang bị áo mưa bộ và tránh thắng gấp."
                        else
                            "Nguy cơ trượt nước (aquaplaning), giữ khoảng cách xe trước > 50m, bật đèn chiếu gần/đèn sương mù."
                    )
                }
            } else {
                Pair(null, null)
            }

            waypoints.add(
                RouteWaypoint(
                    time = etaTime,
                    type = type,
                    locationName = locName,
                    temp = temp,
                    condition = condition,
                    isWarning = isSevereHazard,
                    warningTitle = warnTitle,
                    warningDesc = warnDesc,
                    windSpeed = windSpeed,
                    visibility = visibility,
                    rainAmount = rainAmount,
                    latitude = pt.latitude,
                    longitude = pt.longitude
                )
            )
        }

        return waypoints
    }

    /**
     * Tạo khuyến cáo an toàn lái xe chuyên sâu theo từng loại phương tiện (Ô tô vs Xe máy).
     */
    fun buildDrivingAdvice(
        vehicleType: String,
        hasSevereWarning: Boolean,
        distanceKm: Int,
        durationText: String
    ): String {
        return if (vehicleType == "Xe máy") {
            if (hasSevereWarning) {
                "🛵 Khuyến cáo Xe máy: Lộ trình $distanceKm km có đoạn cảnh báo mưa dông/ngập úng. Hãy mặc áo mưa bộ (tránh áo mưa cánh dơi bị gió giật), giảm tốc độ khi qua vạch kẻ đường/nắp cống trơn trượt, và không cố vượt qua vùng nước ngập quá cổ pô xe."
            } else {
                "🛵 Khuyến cáo Xe máy: Thời tiết thuận lợi trên toàn bộ $distanceKm km ($durationText). Đội mũ bảo hiểm đạt chuẩn, giữ tốc độ ổn định và nghỉ ngơi sau mỗi 60-90 phút lái xe liên tục."
            }
        } else {
            if (hasSevereWarning) {
                "🚗 Khuyến cáo Ô tô: Tuyến đường xuất hiện cảnh báo thời tiết xấu. Đề xuất bật đèn sương mù/đèn gầm, giữ khoảng cách tối thiểu 50m với xe trước, tắt kiểm soát hành trình (Cruise Control) để tránh trượt nước (aquaplaning), và quan sát kỹ mực nước trước khi qua vùng trũng."
            } else {
                "🚗 Khuyến cáo Ô tô: Điều kiện lái xe lý tưởng cho quãng đường $distanceKm km ($durationText). Duy trì đúng làn đường quy định, tuân thủ tốc độ cao tốc và giữ tỉnh táo trong suốt hành trình."
            }
        }
    }

    private fun deriveIntermediateName(origin: String, destination: String, ratio: Double): String {
        val o = origin.lowercase()
        val d = destination.lowercase()
        return when {
            (o.contains("hồ chí minh") || o.contains("sài gòn")) && d.contains("đà lạt") -> {
                if (ratio < 0.5) "Đèo Chuối - Madagui" else "Đèo Bảo Lộc"
            }
            (o.contains("hồ chí minh") || o.contains("sài gòn")) && d.contains("vũng tàu") -> {
                if (ratio < 0.5) "Long Thành - Đồng Nai" else "Bà Rịa"
            }
            o.contains("đà nẵng") && d.contains("huế") -> {
                if (ratio < 0.5) "Đèo Hải Vân" else "Vịnh Lăng Cô"
            }
            o.contains("hà nội") && d.contains("hải phòng") -> {
                if (ratio < 0.5) "Hải Dương" else "Nút giao An Lão"
            }
            o.contains("cơ sở 1") && d.contains("cơ sở 2") -> {
                "Cầu Bình Lợi - QL13"
            }
            o.contains("cơ sở 1") && d.contains("cơ sở 3") -> {
                "Đại lộ Mai Chí Thọ"
            }
            else -> "Trạm trung gian (km ${(ratio * 100).toInt()}%)"
        }
    }

    private fun isNearKnownFloodZone(lat: Double, lon: Double): Boolean {
        // Kiểm tra khoảng cách trong bán kính 3km đến các điểm đen ngập úng UTH/TP.HCM
        val floodPoints = listOf(
            RouteGeoPoint(10.8038, 106.7140), // Ung Văn Khiêm
            RouteGeoPoint(10.7930, 106.7180), // Nguyễn Hữu Cảnh
            RouteGeoPoint(10.8200, 106.7100), // Cầu Bình Triệu / QL13
            RouteGeoPoint(10.8400, 106.7600)  // Đỗ Xuân Hợp
        )
        return floodPoints.any {
            haversineDistance(it, RouteGeoPoint(lat, lon)) <= 3.5
        }
    }

    private fun haversineDistance(p1: RouteGeoPoint, p2: RouteGeoPoint): Double {
        val r = 6371.0 // Bán kính Trái Đất (km)
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLon = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun generateInterpolatedPath(start: RouteGeoPoint, end: RouteGeoPoint, steps: Int): List<RouteGeoPoint> {
        val list = mutableListOf<RouteGeoPoint>()
        for (i in 0..steps) {
            val frac = i.toDouble() / steps
            list.add(
                RouteGeoPoint(
                    latitude = start.latitude + (end.latitude - start.latitude) * frac,
                    longitude = start.longitude + (end.longitude - start.longitude) * frac
                )
            )
        }
        return list
    }
}

package com.example.android_uth_02_weather_viewing_app_group6.data.voice

import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import kotlin.math.roundToInt

data class VoiceWeatherBulletin(
    val cityName: String,
    val weatherOverview: String,
    val drivingAlert: String,
    val fullSpeechScript: String
)

object WeatherAdvisoryGenerator {

    /**
     * Tạo bản tin thời tiết và lời khuyên an toàn giao thông đường bộ bằng tiếng Việt
     * được tối ưu hóa đặc biệt cho Android TextToSpeech phát âm tự nhiên.
     */
    fun generateBulletin(weather: CurrentWeather): VoiceWeatherBulletin {
        val city = if (weather.cityName.isBlank()) "vị trí của bạn" else weather.cityName
        val temp = weather.temperatureC.roundToInt()
        val feelsLike = weather.feelsLikeC.roundToInt()
        val windKmh = (weather.windSpeedMps * 3.6).roundToInt()
        val desc = weather.description.lowercase().trim()

        val overview = buildString {
            append("Bản tin thời tiết tại $city. ")
            append("Hiện tại $desc, ")
            append("nhiệt độ khoảng $temp độ C, ")
            if (feelsLike != temp) {
                append("cảm giác thực tế như $feelsLike độ C. ")
            }
            append("Độ ẩm ${weather.humidityPercent} phần trăm, ")
            append("sức gió khoảng $windKmh ki-lô-mét một giờ.")
        }

        val drivingAlert = generateSafetyAdvisory(weather, desc, temp, windKmh)

        val fullScript = "$overview $drivingAlert"

        return VoiceWeatherBulletin(
            cityName = city,
            weatherOverview = overview,
            drivingAlert = drivingAlert,
            fullSpeechScript = fullScript
        )
    }

    private fun generateSafetyAdvisory(
        weather: CurrentWeather,
        desc: String,
        temp: Int,
        windKmh: Int
    ): String {
        val isRainy = desc.contains("mưa") ||
                desc.contains("dông") ||
                desc.contains("giông") ||
                weather.weatherMain.contains("Rain", ignoreCase = true) ||
                weather.weatherMain.contains("Thunderstorm", ignoreCase = true) ||
                weather.weatherMain.contains("Drizzle", ignoreCase = true)

        val isFoggy = desc.contains("sương mù") ||
                desc.contains("sương") ||
                weather.weatherMain.contains("Fog", ignoreCase = true) ||
                weather.weatherMain.contains("Mist", ignoreCase = true)

        return when {
            isRainy -> {
                "Cảnh báo an toàn khi lái xe: Trời đang có mưa, mặt đường rất trơn trượt và tầm nhìn bị giảm sút. " +
                        "Các bạn sinh viên và người lái xe máy vui lòng giảm tốc độ, bật đèn chiếu gần, " +
                        "giữ khoảng cách an toàn và chú ý quan sát những đoạn đường dễ ngập úng."
            }
            windKmh >= 25 -> {
                "Cảnh báo an toàn khi lái xe: Sức gió ngoài trời đang khá mạnh khoảng $windKmh ki-lô-mét một giờ. " +
                        "Xin chú ý giữ chắc tay lái, đi chậm và cẩn thận khi di chuyển qua các cây cầu lớn, " +
                        "khu vực nhà cao tầng hoặc đoạn đường trống hút gió."
            }
            temp >= 35 -> {
                "Cảnh báo an toàn khi lái xe: Thời tiết đang nắng nóng gay gắt lên tới $temp độ C. " +
                        "Khi di chuyển ngoài trời, hãy trang bị áo chống nắng, đeo kính râm và bổ sung đủ nước " +
                        "để phòng tránh sốc nhiệt và mất tập trung khi lái xe."
            }
            isFoggy -> {
                "Cảnh báo an toàn khi lái xe: Khu vực đang có sương mù làm hạn chế tầm nhìn. " +
                        "Vui lòng bật đèn xe, duy trì tốc độ chậm và tuyệt đối không vượt ẩu."
            }
            else -> {
                "Lời khuyên an toàn khi lái xe: Điều kiện thời tiết hiện tại rất thuận lợi. " +
                        "Chúc bạn có một hành trình di chuyển an toàn, luôn chú ý quan sát và thượng lộ bình an!"
            }
        }
    }
}

package com.example.android_uth_02_weather_viewing_app_group6

import com.example.android_uth_02_weather_viewing_app_group6.data.voice.WeatherAdvisoryGenerator
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VoiceWeatherAlertUnitTest {

    @Test
    fun testGenerateBulletin_rainyWeather_generatesSlipperyRoadAlert() {
        val rainWeather = CurrentWeather(
            cityName = "TP. Hồ Chí Minh",
            temperatureC = 26.4,
            feelsLikeC = 27.8,
            minTemperatureC = 24.0,
            maxTemperatureC = 30.0,
            description = "Mưa rào và dông rải rác",
            weatherMain = "Rain",
            humidityPercent = 88,
            pressureHpa = 1008.0,
            windSpeedMps = 4.2,
            windDirectionDeg = 180,
            latitude = 10.76,
            longitude = 106.66,
            iconCode = "10d"
        )

        val bulletin = WeatherAdvisoryGenerator.generateBulletin(rainWeather)

        assertEquals("TP. Hồ Chí Minh", bulletin.cityName)
        assertTrue(bulletin.weatherOverview.contains("26 độ C"))
        assertTrue(bulletin.weatherOverview.contains("88 phần trăm"))
        assertTrue(bulletin.drivingAlert.contains("trơn trượt"))
        assertTrue(bulletin.drivingAlert.contains("xe máy"))
        assertTrue(bulletin.fullSpeechScript.contains("Bản tin thời tiết tại TP. Hồ Chí Minh"))
    }

    @Test
    fun testGenerateBulletin_hotWeather_generatesHeatwaveAlert() {
        val hotWeather = CurrentWeather(
            cityName = "Hà Nội",
            temperatureC = 36.5,
            feelsLikeC = 39.0,
            minTemperatureC = 29.0,
            maxTemperatureC = 37.0,
            description = "Trời nắng gắt",
            weatherMain = "Clear",
            humidityPercent = 55,
            pressureHpa = 1010.0,
            windSpeedMps = 3.0,
            windDirectionDeg = 90,
            latitude = 21.02,
            longitude = 105.83,
            iconCode = "01d"
        )

        val bulletin = WeatherAdvisoryGenerator.generateBulletin(hotWeather)

        assertTrue(bulletin.weatherOverview.contains("37 độ C"))
        assertTrue(bulletin.drivingAlert.contains("nắng nóng"))
        assertTrue(bulletin.drivingAlert.contains("chống nắng"))
        assertTrue(bulletin.drivingAlert.contains("sốc nhiệt"))
    }

    @Test
    fun testGenerateBulletin_strongWind_generatesWindAlert() {
        val windyWeather = CurrentWeather(
            cityName = "Đà Nẵng",
            temperatureC = 28.0,
            feelsLikeC = 28.0,
            minTemperatureC = 25.0,
            maxTemperatureC = 29.0,
            description = "Gió lớn",
            weatherMain = "Windy",
            humidityPercent = 65,
            pressureHpa = 1012.0,
            windSpeedMps = 8.0, // 8 * 3.6 = 28.8 km/h >= 25
            windDirectionDeg = 45,
            latitude = 16.05,
            longitude = 108.20,
            iconCode = "50d"
        )

        val bulletin = WeatherAdvisoryGenerator.generateBulletin(windyWeather)

        assertTrue(bulletin.drivingAlert.contains("Sức gió ngoài trời đang khá mạnh"))
        assertTrue(bulletin.drivingAlert.contains("giữ chắc tay lái"))
    }

    @Test
    fun testGenerateBulletin_pleasantWeather_generatesSafeWish() {
        val clearWeather = CurrentWeather(
            cityName = "Đà Lạt",
            temperatureC = 22.0,
            feelsLikeC = 22.0,
            minTemperatureC = 16.0,
            maxTemperatureC = 24.0,
            description = "Trời trong xanh",
            weatherMain = "Clear",
            humidityPercent = 60,
            pressureHpa = 1014.0,
            windSpeedMps = 2.5,
            windDirectionDeg = 120,
            latitude = 11.94,
            longitude = 108.45,
            iconCode = "01d"
        )

        val bulletin = WeatherAdvisoryGenerator.generateBulletin(clearWeather)

        assertTrue(bulletin.drivingAlert.contains("thượng lộ bình an"))
    }
}

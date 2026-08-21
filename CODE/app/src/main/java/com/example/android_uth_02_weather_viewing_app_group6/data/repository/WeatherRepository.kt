package com.example.android_uth_02_weather_viewing_app_group6.data.repository

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeocodingApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.WeatherApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResult
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenMeteoWeatherResponse
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather

class WeatherRepository(
    private val weatherApi: WeatherApiService,
    private val geocodingApi: GeocodingApiService,
) {

    suspend fun getCurrentWeather(cityName: String): Result<CurrentWeather> {
        return try {
            val locationResponse = geocodingApi.searchCity(cityName.trim())

            if (!locationResponse.isSuccessful) {
                return Result.failure(
                    IllegalStateException("Không thể tìm kiếm thành phố (HTTP ${locationResponse.code()}).")
                )
            }

            val location = locationResponse.body()?.results?.firstOrNull()
                ?: return Result.failure(
                    IllegalArgumentException("Không tìm thấy thành phố: $cityName")
                )

            val weatherResponse = weatherApi.getCurrentWeather(
                latitude = location.latitude,
                longitude = location.longitude,
            )

            if (!weatherResponse.isSuccessful) {
                return Result.failure(
                    IllegalStateException("Open-Meteo trả về lỗi HTTP ${weatherResponse.code()}.")
                )
            }

            val body = weatherResponse.body()
                ?: return Result.failure(IllegalStateException("API không trả về dữ liệu thời tiết."))

            Result.success(body.toCurrentWeather(location))
        } catch (e: Exception) {
            Result.failure(
                IllegalStateException(
                    e.localizedMessage ?: "Không thể kết nối đến Open-Meteo.",
                    e,
                )
            )
        }
    }

    private fun OpenMeteoWeatherResponse.toCurrentWeather(
        location: GeocodingResult,
    ): CurrentWeather {
        val code = current.weatherCode
        val min = daily?.minTemperature?.firstOrNull() ?: current.temperature
        val max = daily?.maxTemperature?.firstOrNull() ?: current.temperature

        return CurrentWeather(
            cityName = location.name,
            temperatureC = current.temperature,
            feelsLikeC = current.apparentTemperature,
            minTemperatureC = min,
            maxTemperatureC = max,
            description = weatherDescription(code),
            weatherMain = weatherMain(code),
            humidityPercent = current.humidity,
            pressureHpa = current.pressure,
            windSpeedMps = current.windSpeed,
            windDirectionDeg = current.windDirection,
            latitude = latitude,
            longitude = longitude,
            iconCode = code.toString(),
        )
    }

    private fun weatherDescription(code: Int): String = when (code) {
        0 -> "Trời quang"
        1 -> "Chủ yếu quang"
        2 -> "Mây rải rác"
        3 -> "Nhiều mây"
        45, 48 -> "Sương mù"
        51, 53, 55 -> "Mưa phùn"
        56, 57 -> "Mưa phùn đóng băng"
        61, 63, 65 -> "Mưa"
        66, 67 -> "Mưa đóng băng"
        71, 73, 75, 77 -> "Tuyết"
        80, 81, 82 -> "Mưa rào"
        85, 86 -> "Mưa tuyết rào"
        95 -> "Dông"
        96, 99 -> "Dông kèm mưa đá"
        else -> "Không xác định"
    }

    private fun weatherMain(code: Int): String = when (code) {
        0, 1 -> "Clear"
        2, 3 -> "Clouds"
        45, 48 -> "Fog"
        in 51..67, in 80..82 -> "Rain"
        in 71..77, 85, 86 -> "Snow"
        95, 96, 99 -> "Thunderstorm"
        else -> "Unknown"
    }
}

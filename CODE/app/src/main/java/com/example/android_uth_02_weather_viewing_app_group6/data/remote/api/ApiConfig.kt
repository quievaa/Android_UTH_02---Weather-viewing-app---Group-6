package com.example.android_uth_02_weather_viewing_app_group6.data.remote.api

/**
 * ==============================================================================
 * ⚙️ TẬP TIN CẤU HÌNH API KEYS & SERVER CHO DỰ ÁN THỜI TIẾT (GROUP 6)
 * ==============================================================================
 * Bạn có thể dán trực tiếp các API Key của mình vào file này để sử dụng ngay
 * mà không cần phải nhập lại nhiều lần trên màn hình cài đặt.
 *
 * 1. OPENWEATHER_API_KEY : Lấy tại https://home.openweathermap.org/users/sign_up
 * 2. WEATHERAPI_KEY      : Lấy tại https://www.weatherapi.com/signup.aspx
 * 3. CUSTOM_API_URL      : URL Server hoặc Proxy API riêng nếu có
 * ==============================================================================
 */
object ApiConfig {

    /**
     * 🔑 API Key cho OpenWeatherMap (https://openweathermap.org/)
     * Dán key của bạn vào giữa 2 dấu ngoặc kép bên dưới.
     */
    const val OPENWEATHER_API_KEY: String = "cfee352fb67d67d899b90f855d652543"

    /**
     * 🔑 API Key cho WeatherAPI.com (https://www.weatherapi.com/)
     * Dán key của bạn vào giữa 2 dấu ngoặc kép bên dưới.
     */
    const val WEATHERAPI_KEY: String = "6316531d1ec14933a8494058260409"

    /**
     * 🌐 URL máy chủ API thời tiết riêng (Custom API Endpoint / Proxy)
     * Ví dụ: "https://my-custom-weather-api.com/v1/"
     */
    const val CUSTOM_API_URL: String = ""

    /**
     * 🔑 API Key / Bearer Token cho máy chủ riêng (nếu có yêu cầu)
     */
    const val CUSTOM_API_KEY: String = ""

    /**
     * 📄 Định dạng dữ liệu của máy chủ riêng:
     * - "OPEN_WEATHER" : Chuẩn dữ liệu OpenWeatherMap
     * - "WEATHER_API"  : Chuẩn dữ liệu WeatherAPI.com
     * - "OPEN_METEO"   : Chuẩn dữ liệu Open-Meteo
     */
    const val CUSTOM_API_FORMAT: String = "OPEN_WEATHER"
}

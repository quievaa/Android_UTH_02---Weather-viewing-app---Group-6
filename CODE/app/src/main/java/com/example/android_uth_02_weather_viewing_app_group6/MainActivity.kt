package com.example.android_uth_02_weather_viewing_app_group6

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.RetrofitClient
import com.example.android_uth_02_weather_viewing_app_group6.ui.navigation.WeatherApp
import com.example.android_uth_02_weather_viewing_app_group6.ui.theme.Android_UTH_02_Weather_Viewing_App_Group6Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Gọi thử nghiệm API để kiểm tra dữ liệu trả về trong Logcat
        testNetworkCall()

        setContent {
            Android_UTH_02_Weather_Viewing_App_Group6Theme {
                WeatherApp()
            }
        }
    }

    private fun testNetworkCall() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getCurrentWeather("Ho Chi Minh")
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    Log.d("WeatherTest", "Thành phố: ${weatherData?.cityName}")
                    Log.d("WeatherTest", "Nhiệt độ: ${weatherData?.main?.temp}°C")
                    Log.d("WeatherTest", "Mô tả: ${weatherData?.weather?.firstOrNull()?.description}")
                } else {
                    Log.e("WeatherTest", "Lỗi API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("WeatherTest", "Lỗi ngoại lệ mạng: ${e.localizedMessage}")
            }
        }
    }
}

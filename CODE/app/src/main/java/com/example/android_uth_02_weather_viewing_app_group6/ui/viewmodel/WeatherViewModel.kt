package com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: CurrentWeather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Trạng thái đơn vị nhiệt độ: true là Celsius, false là Fahrenheit
    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    // Trạng thái đơn vị gió: "m/s" hoặc "km/h"
    private val _windUnit = MutableStateFlow("m/s")
    val windUnit: StateFlow<String> = _windUnit.asStateFlow()

    private var lastCity: String = "Ho Chi Minh"

    init {
        loadCurrentWeather(lastCity)
    }

    fun loadCurrentWeather(cityName: String) {
        val city = cityName.trim()
        if (city.isBlank()) {
            _uiState.value = WeatherUiState.Error("Vui lòng nhập tên thành phố.")
            return
        }

        lastCity = city
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getCurrentWeather(city)
                .onSuccess { _uiState.value = WeatherUiState.Success(it) }
                .onFailure {
                    _uiState.value = WeatherUiState.Error(
                        it.message ?: "Đã xảy ra lỗi khi tải dữ liệu thời tiết."
                    )
                }
        }
    }

    fun toggleTemperatureUnit() {
        _isCelsius.value = !_isCelsius.value
    }

    fun updateWindUnit(unit: String) {
        _windUnit.value = unit
    }

    /**
     * Chuyển đổi nhiệt độ dựa trên đơn vị hiện tại
     */
    fun formatTemperature(tempC: Double): String {
        return if (_isCelsius.value) {
            "${tempC.toInt()}°C"
        } else {
            val tempF = tempC * 9 / 5 + 32
            "${tempF.toInt()}°F"
        }
    }

    /**
     * Chuyển đổi tốc độ gió dựa trên đơn vị hiện tại
     */
    fun formatWindSpeed(speedMps: Double): String {
        return if (_windUnit.value == "m/s") {
            String.format(Locale.US, "%.1f m/s", speedMps)
        } else {
            val speedKmh = speedMps * 3.6
            String.format(Locale.US, "%.1f km/h", speedKmh)
        }
    }

    fun retry() {
        loadCurrentWeather(lastCity)
    }
}

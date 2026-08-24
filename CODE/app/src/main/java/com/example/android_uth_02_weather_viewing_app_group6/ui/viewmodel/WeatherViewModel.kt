package com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_uth_02_weather_viewing_app_group6.data.location.LocationTracker
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

    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    private val _windUnit = MutableStateFlow("m/s")
    val windUnit: StateFlow<String> = _windUnit.asStateFlow()

    private var lastCity: String = "Ho Chi Minh"
    private var lastCoordinates: Pair<Double, Double>? = null

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
        lastCoordinates = null
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

    fun loadWeatherByCoordinates(latitude: Double, longitude: Double, cityName: String? = null) {
        lastCoordinates = Pair(latitude, longitude)
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getWeatherByCoordinates(latitude, longitude, cityName)
                .onSuccess {
                    lastCity = it.cityName
                    _uiState.value = WeatherUiState.Success(it)
                }
                .onFailure {
                    _uiState.value = WeatherUiState.Error(
                        it.message ?: "Đã xảy ra lỗi khi tải dữ liệu từ vị trí GPS."
                    )
                }
        }
    }

    fun fetchLocationWeather(
        locationTracker: LocationTracker,
        onResult: (Boolean, String?) -> Unit = { _, _ -> },
    ) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                repository.getWeatherByCoordinates(location.latitude, location.longitude, "Vị trí hiện tại")
                    .onSuccess {
                        lastCity = it.cityName
                        lastCoordinates = Pair(location.latitude, location.longitude)
                        _uiState.value = WeatherUiState.Success(it)
                        onResult(true, null)
                    }
                    .onFailure { err ->
                        val msg = err.message ?: "Không thể lấy thời tiết từ GPS."
                        _uiState.value = WeatherUiState.Error(msg)
                        onResult(false, msg)
                    }
            } else {
                val msg = "Không thể lấy vị trí GPS hiện tại. Vui lòng bật vị trí/GPS và cấp quyền."
                _uiState.value = WeatherUiState.Error(msg)
                onResult(false, msg)
            }
        }
    }

    fun toggleTemperatureUnit() {
        _isCelsius.value = !_isCelsius.value
    }

    fun updateWindUnit(unit: String) {
        _windUnit.value = unit
    }

    fun formatTemperature(tempC: Double): String {
        return if (_isCelsius.value) {
            "${tempC.toInt()}°C"
        } else {
            val tempF = tempC * 9 / 5 + 32
            "${tempF.toInt()}°F"
        }
    }

    fun formatWindSpeed(speedMps: Double): String {
        return if (_windUnit.value == "m/s") {
            String.format(Locale.US, "%.1f m/s", speedMps)
        } else {
            val speedKmh = speedMps * 3.6
            String.format(Locale.US, "%.1f km/h", speedKmh)
        }
    }

    fun retry() {
        val coords = lastCoordinates
        if (coords != null) {
            loadWeatherByCoordinates(coords.first, coords.second)
        } else {
            loadCurrentWeather(lastCity)
        }
    }
}

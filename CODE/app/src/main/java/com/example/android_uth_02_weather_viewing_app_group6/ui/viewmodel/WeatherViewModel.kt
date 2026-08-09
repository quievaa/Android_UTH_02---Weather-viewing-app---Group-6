package com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    fun retry() {
        loadCurrentWeather(lastCity)
    }
}

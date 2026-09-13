package com.example.android_uth_02_weather_viewing_app_group6.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.AppPreferences
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiCustomKnowledgeRepository
import com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel.WeatherViewModel

class WeatherViewModelFactory(
    private val repository: WeatherRepository = WeatherRepository(),
    private val appPreferences: AppPreferences? = null,
    private val customKnowledgeRepo: AiCustomKnowledgeRepository? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(repository, appPreferences, customKnowledgeRepo = customKnowledgeRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

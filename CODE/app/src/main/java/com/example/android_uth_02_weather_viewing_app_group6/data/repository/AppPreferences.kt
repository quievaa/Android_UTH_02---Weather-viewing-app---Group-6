package com.example.android_uth_02_weather_viewing_app_group6.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.android_uth_02_weather_viewing_app_group6.data.model.FavoriteCity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")

class AppPreferences(private val context: Context) {

    private val gson = Gson()

    companion object {
        val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        val KEY_WIND_UNIT = stringPreferencesKey("wind_unit")
        val KEY_FAVORITES = stringPreferencesKey("favorite_cities")
        val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")

        // API Resiliency & Multi-API Settings
        val KEY_MULTI_API_ENABLED = booleanPreferencesKey("multi_api_enabled")
        val KEY_PRIMARY_API_PROVIDER = stringPreferencesKey("primary_api_provider")
        val KEY_WEATHER_API_KEY = stringPreferencesKey("weather_api_key")
        val KEY_OPEN_WEATHER_KEY = stringPreferencesKey("open_weather_key")

        // Custom API Endpoint Settings (No-Code Configuration)
        val KEY_CUSTOM_ENDPOINT_URL = stringPreferencesKey("custom_endpoint_url")
        val KEY_CUSTOM_ENDPOINT_NAME = stringPreferencesKey("custom_endpoint_name")
        val KEY_CUSTOM_ENDPOINT_ENABLED = booleanPreferencesKey("custom_endpoint_enabled")
        val KEY_CUSTOM_ENDPOINT_KEY = stringPreferencesKey("custom_endpoint_key")
        val KEY_CUSTOM_ENDPOINT_FORMAT = stringPreferencesKey("custom_endpoint_format")
    }

    // Đơn vị nhiệt độ (°C / °F)
    val temperatureUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_TEMP_UNIT] ?: "C"
    }

    suspend fun saveTemperatureUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TEMP_UNIT] = unit
        }
    }

    // Đơn vị tốc độ gió (m/s / km/h)
    val windUnit: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WIND_UNIT] ?: "m/s"
    }

    suspend fun saveWindUnit(unit: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WIND_UNIT] = unit
        }
    }

    // Cấu hình Multi-API (Chạy song song dự phòng)
    val isMultiApiEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_MULTI_API_ENABLED] ?: true
    }

    suspend fun saveMultiApiEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MULTI_API_ENABLED] = enabled
        }
    }

    // Nhà cung cấp chính (OPEN_METEO, WEATHER_API, OPEN_WEATHER, CUSTOM_ENDPOINT)
    val primaryApiProvider: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_PRIMARY_API_PROVIDER] ?: "OPEN_METEO"
    }

    suspend fun savePrimaryApiProvider(provider: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRIMARY_API_PROVIDER] = provider
        }
    }

    // API Key tùy chỉnh cho các dịch vụ mặc định
    val weatherApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_WEATHER_API_KEY] ?: ""
    }

    suspend fun saveWeatherApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEATHER_API_KEY] = key
        }
    }

    val openWeatherKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_OPEN_WEATHER_KEY] ?: ""
    }

    suspend fun saveOpenWeatherKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_OPEN_WEATHER_KEY] = key
        }
    }

    // ========================================================
    // Cấu hình Custom API Endpoint (Thay đổi API không cần code)
    // ========================================================
    val customEndpointUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_ENDPOINT_URL] ?: ""
    }

    val customEndpointName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_ENDPOINT_NAME] ?: "Custom API Server"
    }

    val customEndpointEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_ENDPOINT_ENABLED] ?: false
    }

    val customEndpointKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_ENDPOINT_KEY] ?: ""
    }

    val customEndpointFormat: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CUSTOM_ENDPOINT_FORMAT] ?: "OPEN_WEATHER" // OPEN_WEATHER, OPEN_METEO, WEATHER_API
    }

    suspend fun saveCustomEndpoint(
        url: String,
        name: String,
        enabled: Boolean,
        apiKey: String,
        format: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CUSTOM_ENDPOINT_URL] = url.trim()
            prefs[KEY_CUSTOM_ENDPOINT_NAME] = name.trim().ifBlank { "Custom API Server" }
            prefs[KEY_CUSTOM_ENDPOINT_ENABLED] = enabled
            prefs[KEY_CUSTOM_ENDPOINT_KEY] = apiKey.trim()
            prefs[KEY_CUSTOM_ENDPOINT_FORMAT] = format
        }
    }

    suspend fun resetCustomEndpoint() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CUSTOM_ENDPOINT_URL)
            prefs.remove(KEY_CUSTOM_ENDPOINT_NAME)
            prefs.remove(KEY_CUSTOM_ENDPOINT_ENABLED)
            prefs.remove(KEY_CUSTOM_ENDPOINT_KEY)
            prefs.remove(KEY_CUSTOM_ENDPOINT_FORMAT)
        }
    }

    // Danh sách địa điểm yêu thích
    val favoriteCities: Flow<List<FavoriteCity>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[KEY_FAVORITES] ?: ""
        if (jsonString.isEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<FavoriteCity>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        }
    }

    suspend fun addFavoriteCity(city: FavoriteCity) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[KEY_FAVORITES] ?: ""
            val type = object : TypeToken<MutableList<FavoriteCity>>() {}.type
            val currentList: MutableList<FavoriteCity> = if (jsonString.isNotEmpty()) {
                gson.fromJson(jsonString, type)
            } else {
                mutableListOf()
            }

            if (currentList.none { it.id == city.id }) {
                currentList.add(city)
                prefs[KEY_FAVORITES] = gson.toJson(currentList)
            }
        }
    }

    suspend fun removeFavoriteCity(cityId: String) {
        context.dataStore.edit { prefs ->
            val jsonString = prefs[KEY_FAVORITES] ?: ""
            if (jsonString.isNotEmpty()) {
                val type = object : TypeToken<MutableList<FavoriteCity>>() {}.type
                val currentList: MutableList<FavoriteCity> = gson.fromJson(jsonString, type)
                currentList.removeAll { it.id == cityId }
                prefs[KEY_FAVORITES] = gson.toJson(currentList)
            }
        }
    }

    // Lịch sử tìm kiếm
    val searchHistory: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val jsonString = prefs[KEY_SEARCH_HISTORY] ?: ""
        if (jsonString.isEmpty()) {
            emptyList()
        } else {
            val type = object : TypeToken<List<String>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyList()
        }
    }

    suspend fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val jsonString = prefs[KEY_SEARCH_HISTORY] ?: ""
            val type = object : TypeToken<MutableList<String>>() {}.type
            val currentList: MutableList<String> = if (jsonString.isNotEmpty()) {
                gson.fromJson(jsonString, type)
            } else {
                mutableListOf()
            }

            currentList.remove(query)
            currentList.add(0, query)

            if (currentList.size > 10) {
                currentList.removeAt(currentList.size - 1)
            }

            prefs[KEY_SEARCH_HISTORY] = gson.toJson(currentList)
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_SEARCH_HISTORY)
        }
    }
}

package com.example.android_uth_02_weather_viewing_app_group6.data.model.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
        val KEY_FAVORITES = stringPreferencesKey("favorite_cities")
        val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")
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
}
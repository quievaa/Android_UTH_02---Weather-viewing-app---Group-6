package com.example.android_uth_02_weather_viewing_app_group6.data.location

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.LruCache
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.RetrofitClient
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Request
import java.util.Locale
import kotlin.coroutines.resume

data class NominatimReverseResponse(
    @SerializedName("display_name") val displayName: String?,
    @SerializedName("address") val address: NominatimAddressDto?,
)

data class NominatimAddressDto(
    @SerializedName("suburb") val suburb: String?,
    @SerializedName("quarter") val quarter: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("city_district") val cityDistrict: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("town") val town: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("country") val country: String?,
)

class ReverseGeocoder(private val context: Context) {

    private val gson = Gson()
    private val client = RetrofitClient.okHttpClient

    // High-performance In-Memory LRU Cache for resolved coordinates (100m quantization)
    private val memoryCache = LruCache<String, String>(128)

    suspend fun resolveLocationName(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        // Quantize coordinate to 3 decimal places (~110 meters) for cache hit efficiency
        val cacheKey = String.format(Locale.US, "%.3f,%.3f", latitude, longitude)
        val cached = memoryCache.get(cacheKey)
        if (!cached.isNullOrBlank()) {
            return@withContext cached
        }

        // Step 1: Try Native Android Geocoder
        val systemResult = resolveWithSystemGeocoder(latitude, longitude)
        if (!systemResult.isNullOrBlank()) {
            memoryCache.put(cacheKey, systemResult)
            return@withContext systemResult
        }

        // Step 2: Fallback to OpenStreetMap Nominatim Reverse API (Vietnamese localization)
        val nominatimResult = resolveWithNominatim(latitude, longitude)
        if (!nominatimResult.isNullOrBlank()) {
            memoryCache.put(cacheKey, nominatimResult)
            return@withContext nominatimResult
        }

        // Step 3: Default clean fallback
        val defaultFallback = "Vị trí GPS (${String.format(Locale.US, "%.2f, %.2f", latitude, longitude)})"
        memoryCache.put(cacheKey, defaultFallback)
        return@withContext defaultFallback
    }

    private suspend fun resolveWithSystemGeocoder(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            if (!Geocoder.isPresent()) return@withContext null
            val geocoder = Geocoder(context, Locale.Builder().setLanguage("vi").setRegion("VN").build())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        if (address != null) {
                            val district = address.subAdminArea ?: address.locality ?: address.subLocality
                            val city = address.adminArea ?: address.locality
                            val formatted = formatPlaceName(district, city)
                            if (continuation.isActive) continuation.resume(formatted)
                        } else {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val address = addresses?.firstOrNull()
                if (address != null) {
                    val district = address.subAdminArea ?: address.locality ?: address.subLocality
                    val city = address.adminArea ?: address.locality
                    formatPlaceName(district, city)
                } else {
                    null
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun resolveWithNominatim(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse?lat=$latitude&lon=$longitude&format=json&addressdetails=1&accept-language=vi"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "WeatherApp-UTH-Group6/1.0 (Android)")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val json = response.body?.string() ?: return@withContext null
            val body = gson.fromJson(json, NominatimReverseResponse::class.java)
            val addr = body.address ?: return@withContext null

            val district = addr.quarter ?: addr.suburb ?: addr.cityDistrict ?: addr.district ?: addr.town
            val city = addr.city ?: addr.state ?: addr.country
            formatPlaceName(district, city)
        } catch (_: Exception) {
            null
        }
    }

    private fun formatPlaceName(district: String?, city: String?): String {
        val cleanDistrict = district?.trim()?.removePrefix("Thành phố ")?.removePrefix("TP. ")
        val cleanCity = city?.trim()

        return when {
            !cleanDistrict.isNullOrBlank() && !cleanCity.isNullOrBlank() -> {
                if (cleanDistrict.equals(cleanCity, ignoreCase = true)) {
                    cleanCity
                } else {
                    "$cleanDistrict, $cleanCity"
                }
            }
            !cleanDistrict.isNullOrBlank() -> cleanDistrict
            !cleanCity.isNullOrBlank() -> cleanCity
            else -> "Vị trí của tôi"
        }
    }
}

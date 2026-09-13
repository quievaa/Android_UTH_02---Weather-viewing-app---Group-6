package com.example.android_uth_02_weather_viewing_app_group6.data.repository

import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.ApiConfig
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.GeocodingApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.OpenWeatherMapApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.RetrofitClient
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.WeatherApiDotComService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.WeatherApiService
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.ApiHealthStatus
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.GeocodingResult
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenMeteoWeatherResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.OpenWeatherMapResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.WeatherApiResponse
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.WttrInResponse
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Request
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class WeatherRepository(
    private val weatherApi: WeatherApiService = RetrofitClient.weatherApi,
    private val geocodingApi: GeocodingApiService = RetrofitClient.geocodingApi,
    private val openWeatherApi: OpenWeatherMapApiService = RetrofitClient.openWeatherApi,
    private val weatherApiDotCom: WeatherApiDotComService = RetrofitClient.weatherApiDotCom,
) {

    private val gson = Gson()
    private val directClient = RetrofitClient.okHttpClient

    private data class CachedWeatherEntry(
        val weather: CurrentWeather,
        val timestamp: Long
    )

    // High performance memory cache with 3-minute TTL to minimize network requests during navigation
    private val weatherMemoryCache = java.util.concurrent.ConcurrentHashMap<String, CachedWeatherEntry>()
    private val CACHE_TTL_MS = 3 * 60 * 1000L

    fun clearCache() {
        weatherMemoryCache.clear()
    }

    // ==========================================
    // MULTI-API FAILOVER / PARALLEL FETCH
    // ==========================================

    suspend fun getCurrentWeather(
        cityName: String,
        isMultiApiEnabled: Boolean = true,
        primaryProvider: String = "OPEN_METEO",
        customWeatherApiKey: String = "",
        customOpenWeatherKey: String = "",
        customEndpointUrl: String = "",
        customEndpointName: String = "Custom API",
        customEndpointEnabled: Boolean = false,
        customEndpointKey: String = "",
        customEndpointFormat: String = "OPEN_WEATHER",
        forceRefresh: Boolean = false,
    ): Result<CurrentWeather> {
        val trimmedCity = cityName.trim()
        if (trimmedCity.isBlank()) {
            return Result.failure(IllegalArgumentException("Tên thành phố không được để trống."))
        }

        val cacheKey = "city_${trimmedCity.lowercase()}_${primaryProvider}_${isMultiApiEnabled}"
        if (!forceRefresh) {
            val cached = weatherMemoryCache[cacheKey]
            if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_TTL_MS) {
                return Result.success(cached.weather)
            }
        }

        // Merge runtime keys with ApiConfig constants (from config file / .env)
        val effWeatherApiKey = customWeatherApiKey.ifBlank { ApiConfig.WEATHERAPI_KEY }
        val effOpenWeatherKey = customOpenWeatherKey.ifBlank { ApiConfig.OPENWEATHER_API_KEY }
        val effCustomUrl = customEndpointUrl.ifBlank { ApiConfig.CUSTOM_API_URL }
        val effCustomKey = customEndpointKey.ifBlank { ApiConfig.CUSTOM_API_KEY }
        val effCustomFormat = if (customEndpointFormat.isNotBlank()) customEndpointFormat else ApiConfig.CUSTOM_API_FORMAT
        val effCustomEnabled = customEndpointEnabled || effCustomUrl.isNotBlank()

        // 1. Custom Endpoint if enabled and prioritized
        if (effCustomEnabled && effCustomUrl.isNotBlank() && primaryProvider == "CUSTOM_ENDPOINT") {
            val customResult = fetchCustomEndpoint(
                url = effCustomUrl,
                query = trimmedCity,
                apiKey = effCustomKey,
                format = effCustomFormat,
                providerName = customEndpointName,
                isCity = true
            )
            if (customResult.isSuccess) {
                customResult.onSuccess { weatherMemoryCache[cacheKey] = CachedWeatherEntry(it, System.currentTimeMillis()) }
                return customResult
            }
            if (!isMultiApiEnabled) return customResult
        }

        val providersOrder = getProviderOrder(primaryProvider, isMultiApiEnabled, effCustomEnabled)

        var lastError: Throwable? = null

        // Try providers in ordered priority until one succeeds
        for (provider in providersOrder) {
            val result = when (provider) {
                "CUSTOM_ENDPOINT" -> fetchCustomEndpoint(
                    url = effCustomUrl,
                    query = trimmedCity,
                    apiKey = effCustomKey,
                    format = effCustomFormat,
                    providerName = customEndpointName,
                    isCity = true
                )
                "OPEN_METEO" -> fetchOpenMeteoByCity(trimmedCity)
                "WEATHER_API" -> {
                    if (effWeatherApiKey.isNotBlank()) {
                        fetchWeatherApiByCity(trimmedCity, effWeatherApiKey)
                    } else {
                        fetchWttrIn(trimmedCity, "WeatherAPI (Wttr)")
                    }
                }
                "OPEN_WEATHER" -> {
                    if (effOpenWeatherKey.isNotBlank()) {
                        fetchOpenWeatherByCity(trimmedCity, effOpenWeatherKey)
                    } else {
                        fetchWttrIn(trimmedCity, "OpenWeather (Wttr)")
                    }
                }
                else -> fetchOpenMeteoByCity(trimmedCity)
            }

            if (result.isSuccess) {
                result.onSuccess { weatherMemoryCache[cacheKey] = CachedWeatherEntry(it, System.currentTimeMillis()) }
                return result
            } else {
                lastError = result.exceptionOrNull()
            }
        }

        // Last-resort fallback: Wttr.in
        val wttrResult = fetchWttrIn(trimmedCity, "Wttr.in Dự Phòng")
        if (wttrResult.isSuccess) {
            wttrResult.onSuccess { weatherMemoryCache[cacheKey] = CachedWeatherEntry(it, System.currentTimeMillis()) }
            return wttrResult
        }

        return Result.failure(
            lastError ?: IllegalStateException("Tất cả các nguồn API ($providersOrder) đều không phản hồi.")
        )
    }

    suspend fun getWeatherByCoordinates(
        latitude: Double,
        longitude: Double,
        cityName: String? = null,
        isMultiApiEnabled: Boolean = true,
        primaryProvider: String = "OPEN_METEO",
        customWeatherApiKey: String = "",
        customOpenWeatherKey: String = "",
        customEndpointUrl: String = "",
        customEndpointName: String = "Custom API",
        customEndpointEnabled: Boolean = false,
        customEndpointKey: String = "",
        customEndpointFormat: String = "OPEN_WEATHER",
        forceRefresh: Boolean = false,
    ): Result<CurrentWeather> {
        val latRound = (latitude * 100).toInt() / 100.0
        val lonRound = (longitude * 100).toInt() / 100.0
        val cacheKey = "geo_${latRound}_${lonRound}_${primaryProvider}_${isMultiApiEnabled}"

        if (!forceRefresh) {
            val cached = weatherMemoryCache[cacheKey]
            if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_TTL_MS) {
                return Result.success(cached.weather)
            }
        }

        // Merge runtime keys with ApiConfig constants (from config file / .env)
        val effWeatherApiKey = customWeatherApiKey.ifBlank { ApiConfig.WEATHERAPI_KEY }
        val effOpenWeatherKey = customOpenWeatherKey.ifBlank { ApiConfig.OPENWEATHER_API_KEY }
        val effCustomUrl = customEndpointUrl.ifBlank { ApiConfig.CUSTOM_API_URL }
        val effCustomKey = customEndpointKey.ifBlank { ApiConfig.CUSTOM_API_KEY }
        val effCustomFormat = if (customEndpointFormat.isNotBlank()) customEndpointFormat else ApiConfig.CUSTOM_API_FORMAT
        val effCustomEnabled = customEndpointEnabled || effCustomUrl.isNotBlank()

        if (effCustomEnabled && effCustomUrl.isNotBlank() && primaryProvider == "CUSTOM_ENDPOINT") {
            val customResult = fetchCustomEndpoint(
                url = effCustomUrl,
                query = "$latitude,$longitude",
                apiKey = effCustomKey,
                format = effCustomFormat,
                providerName = customEndpointName,
                isCity = false,
                lat = latitude,
                lon = longitude,
                cityName = cityName
            )
            if (customResult.isSuccess) {
                customResult.onSuccess { weatherMemoryCache[cacheKey] = CachedWeatherEntry(it, System.currentTimeMillis()) }
                return customResult
            }
            if (!isMultiApiEnabled) return customResult
        }

        val providersOrder = getProviderOrder(primaryProvider, isMultiApiEnabled, effCustomEnabled)

        var lastError: Throwable? = null

        for (provider in providersOrder) {
            val result = when (provider) {
                "CUSTOM_ENDPOINT" -> fetchCustomEndpoint(
                    url = effCustomUrl,
                    query = "$latitude,$longitude",
                    apiKey = effCustomKey,
                    format = effCustomFormat,
                    providerName = customEndpointName,
                    isCity = false,
                    lat = latitude,
                    lon = longitude,
                    cityName = cityName
                )
                "OPEN_METEO" -> fetchOpenMeteoByCoords(latitude, longitude, cityName)
                "WEATHER_API" -> {
                    if (effWeatherApiKey.isNotBlank()) {
                        fetchWeatherApiByCoords(latitude, longitude, cityName, effWeatherApiKey)
                    } else {
                        fetchOpenMeteoByCoords(latitude, longitude, cityName)
                    }
                }
                "OPEN_WEATHER" -> {
                    if (effOpenWeatherKey.isNotBlank()) {
                        fetchOpenWeatherByCoords(latitude, longitude, cityName, effOpenWeatherKey)
                    } else {
                        fetchOpenMeteoByCoords(latitude, longitude, cityName)
                    }
                }
                else -> fetchOpenMeteoByCoords(latitude, longitude, cityName)
            }

            if (result.isSuccess) {
                result.onSuccess { weatherMemoryCache[cacheKey] = CachedWeatherEntry(it, System.currentTimeMillis()) }
                return result
            } else {
                lastError = result.exceptionOrNull()
            }
        }

        return Result.failure(
            lastError ?: IllegalStateException("Tất cả các nguồn API đều không phản hồi cho vị trí GPS.")
        )
    }

    private fun getProviderOrder(
        primaryProvider: String,
        isMultiApiEnabled: Boolean,
        customEndpointEnabled: Boolean
    ): List<String> {
        val primary = when (primaryProvider) {
            "CUSTOM_ENDPOINT" -> "CUSTOM_ENDPOINT"
            "WEATHER_API" -> "WEATHER_API"
            "OPEN_WEATHER" -> "OPEN_WEATHER"
            else -> "OPEN_METEO"
        }

        if (!isMultiApiEnabled) {
            return listOf(primary)
        }

        val all = mutableListOf("OPEN_METEO", "WEATHER_API", "OPEN_WEATHER")
        if (customEndpointEnabled && !all.contains("CUSTOM_ENDPOINT")) {
            all.add(0, "CUSTOM_ENDPOINT")
        }

        return listOf(primary) + all.filter { it != primary }
    }

    // ==========================================
    // 0. WTTR.IN FREE KEYLESS PROVIDER
    // ==========================================

    private suspend fun fetchWttrIn(query: String, providerLabel: String): Result<CurrentWeather> = withContext(Dispatchers.IO) {
        try {
            val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
            val url = "https://wttr.in/$encodedQuery?format=j1"
            val request = Request.Builder().url(url).header("User-Agent", "Mozilla/5.0").build()
            val response = directClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(IllegalStateException("Wttr.in HTTP ${response.code}"))
            }

            val json = response.body?.string() ?: return@withContext Result.failure(IllegalStateException("Wttr.in rỗng"))
            val body = gson.fromJson(json, WttrInResponse::class.java)
            val curr = body.currentCondition?.firstOrNull() ?: return@withContext Result.failure(IllegalStateException("Wttr.in không có dữ liệu hiện tại"))
            val area = body.nearestArea?.firstOrNull()

            val temp = curr.tempC?.toDoubleOrNull() ?: 30.0
            val feelsLike = curr.feelsLikeC?.toDoubleOrNull() ?: temp
            val humidity = curr.humidity?.toIntOrNull() ?: 70
            val pressure = curr.pressure?.toDoubleOrNull() ?: 1010.0
            val windSpeedMps = (curr.windspeedKmph?.toDoubleOrNull() ?: 10.0) / 3.6
            val desc = curr.weatherDesc?.firstOrNull()?.value ?: "Trời quang"
            val lat = area?.latitude?.toDoubleOrNull()
            val lon = area?.longitude?.toDoubleOrNull()

            val weather = CurrentWeather(
                cityName = area?.areaName?.firstOrNull()?.value ?: query,
                temperatureC = temp,
                feelsLikeC = feelsLike,
                minTemperatureC = temp - 2.0,
                maxTemperatureC = temp + 2.0,
                description = desc,
                weatherMain = "Clear",
                humidityPercent = humidity,
                pressureHpa = pressure,
                windSpeedMps = windSpeedMps,
                windDirectionDeg = curr.winddirDegree?.toIntOrNull(),
                latitude = lat,
                longitude = lon,
                iconCode = curr.weatherCode,
                apiProvider = providerLabel
            )

            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // CUSTOM API ENDPOINT DYNAMIC CLIENT
    // ==========================================

    private suspend fun fetchCustomEndpoint(
        url: String,
        query: String,
        apiKey: String,
        format: String,
        providerName: String,
        isCity: Boolean,
        lat: Double? = null,
        lon: Double? = null,
        cityName: String? = null
    ): Result<CurrentWeather> = withContext(Dispatchers.IO) {
        try {
            val fullUrl = buildCustomUrl(url, query, apiKey, format, isCity, lat, lon)
            val request = Request.Builder().url(fullUrl).build()
            val response = directClient.newCall(request).execute()

            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    IllegalStateException("$providerName trả về HTTP ${response.code}")
                )
            }

            val json = response.body?.string()
                ?: return@withContext Result.failure(IllegalStateException("$providerName không có dữ liệu"))

            val currentWeather = when (format) {
                "WEATHER_API" -> {
                    val body = gson.fromJson(json, WeatherApiResponse::class.java)
                    body.toCurrentWeather().copy(apiProvider = providerName)
                }
                "OPEN_METEO" -> {
                    val body = gson.fromJson(json, OpenMeteoWeatherResponse::class.java)
                    val dummyLoc = GeocodingResult(cityName ?: query, lat ?: 0.0, lon ?: 0.0, "", null)
                    body.toCurrentWeather(dummyLoc).copy(apiProvider = providerName)
                }
                else -> { // Default OpenWeather format
                    val body = gson.fromJson(json, OpenWeatherMapResponse::class.java)
                    body.toCurrentWeather(cityName ?: query).copy(apiProvider = providerName)
                }
            }

            Result.success(currentWeather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildCustomUrl(
        baseUrl: String,
        query: String,
        apiKey: String,
        format: String,
        isCity: Boolean,
        lat: Double?,
        lon: Double?
    ): String {
        val trimmedBase = baseUrl.trim()
        val sep = if (trimmedBase.contains("?")) "&" else "?"

        return when (format) {
            "WEATHER_API" -> {
                val q = if (isCity) query else "$lat,$lon"
                if (trimmedBase.contains("current.json")) {
                    "$trimmedBase${sep}key=$apiKey&q=$q&lang=vi"
                } else {
                    val base = if (trimmedBase.endsWith("/")) trimmedBase else "$trimmedBase/"
                    "${base}v1/current.json?key=$apiKey&q=$q&lang=vi"
                }
            }
            "OPEN_METEO" -> {
                val lt = lat ?: 10.82
                val ln = lon ?: 106.63
                if (trimmedBase.contains("forecast")) {
                    "$trimmedBase${sep}latitude=$lt&longitude=$ln&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,pressure_msl&daily=temperature_2m_min,temperature_2m_max&timezone=auto"
                } else {
                    val base = if (trimmedBase.endsWith("/")) trimmedBase else "$trimmedBase/"
                    "${base}v1/forecast?latitude=$lt&longitude=$ln&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m,wind_direction_10m,pressure_msl&daily=temperature_2m_min,temperature_2m_max&timezone=auto"
                }
            }
            else -> { // OpenWeather compatible
                val locationParams = if (isCity) "q=$query" else "lat=$lat&lon=$lon"
                val keyParam = if (apiKey.isNotBlank()) "&appid=$apiKey" else ""
                if (trimmedBase.contains("weather")) {
                    "$trimmedBase$sep$locationParams$keyParam&units=metric&lang=vi"
                } else {
                    val base = if (trimmedBase.endsWith("/")) trimmedBase else "$trimmedBase/"
                    "${base}data/2.5/weather?$locationParams$keyParam&units=metric&lang=vi"
                }
            }
        }
    }

    // ==========================================
    // 1. OPEN-METEO PROVIDER
    // ==========================================

    private fun removeVietnameseAccents(str: String): String {
        val nfdNormalizedString = java.text.Normalizer.normalize(str, java.text.Normalizer.Form.NFD)
        val pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
        return pattern.matcher(nfdNormalizedString)
            .replaceAll("")
            .replace("đ", "d")
            .replace("Đ", "D")
            .replace("TP.", "Thanh pho")
            .replace("Tp.", "Thanh pho")
            .trim()
    }

    private suspend fun fetchOpenMeteoByCity(cityName: String): Result<CurrentWeather> {
        return try {
            val locationResponse = geocodingApi.searchCity(cityName)
            var location = if (locationResponse.isSuccessful) {
                locationResponse.body()?.results?.firstOrNull()
            } else null

            // If not found with diacritics, try normalized ASCII (e.g. "Hà Nội" -> "Ha Noi")
            if (location == null) {
                val cleanName = removeVietnameseAccents(cityName)
                if (cleanName.isNotBlank() && cleanName != cityName) {
                    val fallbackRes = geocodingApi.searchCity(cleanName)
                    if (fallbackRes.isSuccessful) {
                        location = fallbackRes.body()?.results?.firstOrNull()
                    }
                }
            }

            if (location != null) {
                fetchOpenMeteoByCoords(location.latitude, location.longitude, cityName)
            } else {
                // Fallback to Wttr.in for flexible global search
                fetchWttrIn(cityName, "Open-Meteo (Wttr)")
            }
        } catch (e: Exception) {
            // Fallback to Wttr.in on geocoding/network exceptions
            fetchWttrIn(cityName, "Open-Meteo (Wttr)")
        }
    }

    private suspend fun fetchOpenMeteoByCoords(
        latitude: Double,
        longitude: Double,
        cityName: String? = null,
    ): Result<CurrentWeather> {
        return try {
            val weatherResponse = weatherApi.getCurrentWeather(
                latitude = latitude,
                longitude = longitude,
            )

            if (!weatherResponse.isSuccessful) {
                return Result.failure(IllegalStateException("Open-Meteo lỗi HTTP ${weatherResponse.code()}"))
            }

            val body = weatherResponse.body()
                ?: return Result.failure(IllegalStateException("Open-Meteo không có dữ liệu"))

            val resolvedName = cityName ?: "Vị trí GPS"
            val dummyLocation = GeocodingResult(
                name = resolvedName,
                latitude = latitude,
                longitude = longitude,
                country = "",
                admin1 = null,
            )

            val weather = body.toCurrentWeather(dummyLocation).copy(apiProvider = "Open-Meteo")
            Result.success(weather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. WEATHERAPI.COM PROVIDER
    // ==========================================

    private suspend fun fetchWeatherApiByCity(cityName: String, apiKey: String): Result<CurrentWeather> {
        return try {
            val response = weatherApiDotCom.getCurrentWeather(
                apiKey = apiKey,
                query = cityName,
            )

            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("WeatherAPI lỗi HTTP ${response.code()}"))
            }

            val body = response.body() ?: return Result.failure(IllegalStateException("WeatherAPI rỗng"))
            Result.success(body.toCurrentWeather())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchWeatherApiByCoords(
        latitude: Double,
        longitude: Double,
        cityName: String?,
        apiKey: String,
    ): Result<CurrentWeather> {
        return try {
            val query = "$latitude,$longitude"
            val response = weatherApiDotCom.getCurrentWeather(
                apiKey = apiKey,
                query = query,
            )

            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("WeatherAPI lỗi HTTP ${response.code()}"))
            }

            val body = response.body() ?: return Result.failure(IllegalStateException("WeatherAPI rỗng"))
            val result = body.toCurrentWeather()
            val finalWeather = if (!cityName.isNullOrBlank()) result.copy(cityName = cityName) else result
            Result.success(finalWeather)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. OPENWEATHERMAP PROVIDER
    // ==========================================

    private suspend fun fetchOpenWeatherByCity(cityName: String, apiKey: String): Result<CurrentWeather> {
        return try {
            val response = openWeatherApi.getWeatherByCity(
                cityName = cityName,
                apiKey = apiKey,
            )

            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("OpenWeatherMap lỗi HTTP ${response.code()}"))
            }

            val body = response.body() ?: return Result.failure(IllegalStateException("OpenWeatherMap rỗng"))
            Result.success(body.toCurrentWeather(cityName))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun fetchOpenWeatherByCoords(
        latitude: Double,
        longitude: Double,
        cityName: String?,
        apiKey: String,
    ): Result<CurrentWeather> {
        return try {
            val response = openWeatherApi.getWeatherByCoordinates(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey,
            )

            if (!response.isSuccessful) {
                return Result.failure(IllegalStateException("OpenWeatherMap lỗi HTTP ${response.code()}"))
            }

            val body = response.body() ?: return Result.failure(IllegalStateException("OpenWeatherMap rỗng"))
            Result.success(body.toCurrentWeather(cityName ?: body.name ?: "Vị trí GPS"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // HEALTH CHECK & PING TEST FOR ALL APIS
    // ==========================================

    suspend fun pingAllApis(
        customWeatherApiKey: String = "",
        customOpenWeatherKey: String = "",
        customEndpointUrl: String = "",
        customEndpointName: String = "Custom API",
        customEndpointEnabled: Boolean = false,
        customEndpointKey: String = "",
        customEndpointFormat: String = "OPEN_WEATHER",
    ): List<ApiHealthStatus> = coroutineScope {
        val effWeatherApiKey = customWeatherApiKey.ifBlank { ApiConfig.WEATHERAPI_KEY }
        val effOpenWeatherKey = customOpenWeatherKey.ifBlank { ApiConfig.OPENWEATHER_API_KEY }
        val effCustomUrl = customEndpointUrl.ifBlank { ApiConfig.CUSTOM_API_URL }
        val effCustomKey = customEndpointKey.ifBlank { ApiConfig.CUSTOM_API_KEY }
        val effCustomFormat = if (customEndpointFormat.isNotBlank()) customEndpointFormat else ApiConfig.CUSTOM_API_FORMAT
        val effCustomEnabled = customEndpointEnabled || effCustomUrl.isNotBlank()

        val openMeteoDeferred = async { pingOpenMeteo() }
        val weatherApiDeferred = async { pingWeatherApi(effWeatherApiKey) }
        val openWeatherDeferred = async { pingOpenWeather(effOpenWeatherKey) }
        val customDeferred = if (effCustomEnabled && effCustomUrl.isNotBlank()) {
            async { pingCustomEndpoint(effCustomUrl, customEndpointName, effCustomKey, effCustomFormat) }
        } else null

        val list = mutableListOf(
            openMeteoDeferred.await(),
            weatherApiDeferred.await(),
            openWeatherDeferred.await()
        )

        customDeferred?.let { list.add(0, it.await()) }
        list
    }

    suspend fun pingCustomEndpoint(
        url: String,
        name: String,
        apiKey: String,
        format: String
    ): ApiHealthStatus = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val fullUrl = buildCustomUrl(url, "Ho Chi Minh", apiKey, format, true, 10.82, 106.63)
            val request = Request.Builder().url(fullUrl).build()
            val response = directClient.newCall(request).execute()
            val latency = System.currentTimeMillis() - start
            if (response.isSuccessful) {
                ApiHealthStatus(
                    providerName = name.ifBlank { "Custom API" },
                    isOnline = true,
                    latencyMs = latency,
                    message = "Kết nối thành công (Tùy chỉnh)",
                )
            } else {
                ApiHealthStatus(
                    providerName = name.ifBlank { "Custom API" },
                    isOnline = false,
                    latencyMs = latency,
                    message = "Lỗi phản hồi HTTP ${response.code}",
                )
            }
        } catch (e: Exception) {
            ApiHealthStatus(
                providerName = name.ifBlank { "Custom API" },
                isOnline = false,
                latencyMs = System.currentTimeMillis() - start,
                message = e.localizedMessage ?: "Không thể kết nối",
            )
        }
    }

    private suspend fun pingOpenMeteo(): ApiHealthStatus {
        val start = System.currentTimeMillis()
        return try {
            val result = withTimeoutOrNull(5000) {
                weatherApi.getCurrentWeather(latitude = 10.82, longitude = 106.63)
            }
            val latency = System.currentTimeMillis() - start
            if (result != null && result.isSuccessful) {
                ApiHealthStatus(
                    providerName = "Open-Meteo",
                    isOnline = true,
                    latencyMs = latency,
                    message = "Hoạt động tốt (Không cần Key)",
                )
            } else {
                ApiHealthStatus(
                    providerName = "Open-Meteo",
                    isOnline = false,
                    latencyMs = latency,
                    message = "Lỗi phản hồi HTTP ${result?.code() ?: "Timeout"}",
                )
            }
        } catch (e: Exception) {
            ApiHealthStatus(
                providerName = "Open-Meteo",
                isOnline = false,
                latencyMs = System.currentTimeMillis() - start,
                message = e.localizedMessage ?: "Không thể kết nối",
            )
        }
    }

    private suspend fun pingWeatherApi(apiKey: String): ApiHealthStatus {
        val start = System.currentTimeMillis()
        if (apiKey.isBlank()) {
            // Test wttr.in fallback
            return try {
                val req = Request.Builder().url("https://wttr.in/Ho+Chi+Minh?format=j1").build()
                val res = directClient.newCall(req).execute()
                val latency = System.currentTimeMillis() - start
                if (res.isSuccessful) {
                    ApiHealthStatus(
                        providerName = "WeatherAPI (Wttr)",
                        isOnline = true,
                        latencyMs = latency,
                        message = "Sẵn sàng (Tự động dự phòng Wttr.in)",
                    )
                } else {
                    ApiHealthStatus(
                        providerName = "WeatherAPI.com",
                        isOnline = false,
                        latencyMs = latency,
                        message = "Chưa nhập API Key riêng",
                    )
                }
            } catch (e: Exception) {
                ApiHealthStatus(
                    providerName = "WeatherAPI.com",
                    isOnline = false,
                    latencyMs = System.currentTimeMillis() - start,
                    message = "Chưa cấu hình Key riêng",
                )
            }
        }

        return try {
            val result = withTimeoutOrNull(5000) {
                weatherApiDotCom.getCurrentWeather(apiKey = apiKey, query = "Ho Chi Minh")
            }
            val latency = System.currentTimeMillis() - start
            if (result != null && result.isSuccessful) {
                ApiHealthStatus(
                    providerName = "WeatherAPI.com",
                    isOnline = true,
                    latencyMs = latency,
                    message = "Hoạt động tốt (Key cá nhân)",
                )
            } else {
                ApiHealthStatus(
                    providerName = "WeatherAPI.com",
                    isOnline = false,
                    latencyMs = latency,
                    message = if (result?.code() == 401 || result?.code() == 403) "Key không hợp lệ/hết hạn" else "HTTP ${result?.code()}",
                )
            }
        } catch (e: Exception) {
            ApiHealthStatus(
                providerName = "WeatherAPI.com",
                isOnline = false,
                latencyMs = System.currentTimeMillis() - start,
                message = e.localizedMessage ?: "Lỗi kết nối",
            )
        }
    }

    private suspend fun pingOpenWeather(apiKey: String): ApiHealthStatus {
        val start = System.currentTimeMillis()
        if (apiKey.isBlank()) {
            // Test wttr.in / open-meteo fallback
            return try {
                val req = Request.Builder().url("https://wttr.in/Ho+Chi+Minh?format=j1").build()
                val res = directClient.newCall(req).execute()
                val latency = System.currentTimeMillis() - start
                if (res.isSuccessful) {
                    ApiHealthStatus(
                        providerName = "OpenWeather (Wttr)",
                        isOnline = true,
                        latencyMs = latency,
                        message = "Sẵn sàng (Tự động dự phòng Wttr.in)",
                    )
                } else {
                    ApiHealthStatus(
                        providerName = "OpenWeatherMap",
                        isOnline = false,
                        latencyMs = latency,
                        message = "Chưa nhập API Key riêng",
                    )
                }
            } catch (e: Exception) {
                ApiHealthStatus(
                    providerName = "OpenWeatherMap",
                    isOnline = false,
                    latencyMs = System.currentTimeMillis() - start,
                    message = "Chưa cấu hình Key riêng",
                )
            }
        }

        return try {
            val result = withTimeoutOrNull(5000) {
                openWeatherApi.getWeatherByCity(cityName = "Ho Chi Minh", apiKey = apiKey)
            }
            val latency = System.currentTimeMillis() - start
            if (result != null && result.isSuccessful) {
                ApiHealthStatus(
                    providerName = "OpenWeatherMap",
                    isOnline = true,
                    latencyMs = latency,
                    message = "Hoạt động tốt (Key cá nhân)",
                )
            } else {
                ApiHealthStatus(
                    providerName = "OpenWeatherMap",
                    isOnline = false,
                    latencyMs = latency,
                    message = if (result?.code() == 401) "Key không hợp lệ/hết hạn" else "HTTP ${result?.code()}",
                )
            }
        } catch (e: Exception) {
            ApiHealthStatus(
                providerName = "OpenWeatherMap",
                isOnline = false,
                latencyMs = System.currentTimeMillis() - start,
                message = e.localizedMessage ?: "Lỗi kết nối",
            )
        }
    }

    // ==========================================
    // DATA MAPPERS
    // ==========================================

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
            apiProvider = "Open-Meteo",
        )
    }

    private fun WeatherApiResponse.toCurrentWeather(): CurrentWeather {
        val curr = current ?: throw IllegalStateException("WeatherAPI current null")
        val loc = location
        val temp = curr.tempC
        val windMps = curr.windKph / 3.6

        return CurrentWeather(
            cityName = loc?.name ?: "Vị trí hiện tại",
            temperatureC = temp,
            feelsLikeC = curr.feelsLikeC,
            minTemperatureC = temp - 2.0,
            maxTemperatureC = temp + 2.0,
            description = curr.condition?.text ?: "Thời tiết ổn định",
            weatherMain = mapConditionCodeToMain(curr.condition?.code ?: 1000),
            humidityPercent = curr.humidity,
            pressureHpa = curr.pressureMb,
            windSpeedMps = windMps,
            windDirectionDeg = curr.windDegree,
            latitude = loc?.lat,
            longitude = loc?.lon,
            iconCode = curr.condition?.code?.toString(),
            apiProvider = "WeatherAPI",
        )
    }

    private fun OpenWeatherMapResponse.toCurrentWeather(fallbackName: String): CurrentWeather {
        val m = main ?: throw IllegalStateException("OpenWeatherMap main null")
        val w = weather?.firstOrNull()
        val wind = wind

        return CurrentWeather(
            cityName = name ?: fallbackName,
            temperatureC = m.temp,
            feelsLikeC = m.feelsLike,
            minTemperatureC = m.tempMin,
            maxTemperatureC = m.tempMax,
            description = w?.description?.replaceFirstChar { it.uppercase() } ?: "Trời quang",
            weatherMain = w?.main ?: "Clear",
            humidityPercent = m.humidity,
            pressureHpa = m.pressure,
            windSpeedMps = wind?.speed ?: 2.0,
            windDirectionDeg = wind?.deg,
            latitude = coord?.lat,
            longitude = coord?.lon,
            iconCode = w?.icon,
            apiProvider = "OpenWeatherMap",
        )
    }

    private fun mapConditionCodeToMain(code: Int): String {
        return when (code) {
            1000 -> "Clear"
            1003, 1006, 1009 -> "Clouds"
            1030, 1135, 1147 -> "Fog"
            1063, 1180, 1183, 1186, 1189, 1192, 1195, 1240, 1243, 1246 -> "Rain"
            1066, 1114, 1210, 1213, 1216, 1219, 1222, 1225 -> "Snow"
            1087, 1273, 1276, 1279, 1282 -> "Thunderstorm"
            else -> "Clouds"
        }
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

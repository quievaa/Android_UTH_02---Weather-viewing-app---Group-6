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
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.HourlyDto
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.DailyDto
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.DailyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.HourlyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import java.util.Locale
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
                if (lastError is IllegalArgumentException) {
                    return result
                }
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
                Result.failure(IllegalArgumentException("Không tìm thấy thành phố: $cityName"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
            val baseWeather = body.toCurrentWeather(cityName)
            val finalWeather = if (baseWeather.latitude != null && baseWeather.longitude != null) {
                try {
                    val meteoRes = weatherApi.getCurrentWeather(baseWeather.latitude, baseWeather.longitude)
                    if (meteoRes.isSuccessful && meteoRes.body() != null) {
                        val mBody = meteoRes.body()!!
                        baseWeather.copy(
                            hourlyForecast = parseHourlyForecast(mBody.hourly),
                            dailyForecast = parseDailyForecast(mBody.daily)
                        )
                    } else baseWeather
                } catch (e: Exception) { baseWeather }
            } else baseWeather

            Result.success(finalWeather)
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
            val baseWeather = body.toCurrentWeather(cityName ?: body.name ?: "Vị trí GPS")
            val finalWeather = try {
                val meteoRes = weatherApi.getCurrentWeather(latitude, longitude)
                if (meteoRes.isSuccessful && meteoRes.body() != null) {
                    val mBody = meteoRes.body()!!
                    baseWeather.copy(
                        hourlyForecast = parseHourlyForecast(mBody.hourly),
                        dailyForecast = parseDailyForecast(mBody.daily)
                    )
                } else baseWeather
            } catch (e: Exception) { baseWeather }

            Result.success(finalWeather)
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
        val hourlyList = parseHourlyForecast(hourly)
        val dailyList = parseDailyForecast(daily)

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
            hourlyForecast = hourlyList,
            dailyForecast = dailyList,
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

    private fun wmoCodeToCondition(code: Int, isNight: Boolean = false): WeatherCondition {
        return when (code) {
            0 -> if (isNight) WeatherCondition.NIGHT_CLEAR else WeatherCondition.SUNNY
            1, 2 -> if (isNight) WeatherCondition.NIGHT_CLOUDY else WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.OVERCAST
            51, 53, 55, 61, 80 -> WeatherCondition.LIGHT_RAIN
            63, 65, 81 -> WeatherCondition.RAIN
            66, 67, 82 -> WeatherCondition.HEAVY_RAIN
            95, 96, 99 -> WeatherCondition.THUNDERSTORM
            else -> if (isNight) WeatherCondition.NIGHT_CLEAR else WeatherCondition.SUNNY
        }
    }

    private fun parseHourlyForecast(hourly: HourlyDto?): List<HourlyForecast> {
        if (hourly == null || hourly.time.isNullOrEmpty() || hourly.temperature.isNullOrEmpty()) {
            return emptyList()
        }
        val times = hourly.time
        val temps = hourly.temperature
        val codes = hourly.weatherCode ?: emptyList()
        val pops = hourly.precipitationProbability ?: emptyList()

        val currentHourStr = try {
            val now = java.time.LocalDateTime.now()
            String.format(Locale.US, "%04d-%02d-%02dT%02d:00", now.year, now.monthValue, now.dayOfMonth, now.hour)
        } catch (e: Exception) { "" }

        var startIndex = times.indexOfFirst { it >= currentHourStr }
        if (startIndex < 0) startIndex = 0

        val result = mutableListOf<HourlyForecast>()
        val maxHours = minOf(startIndex + 24, times.size)

        for (i in startIndex until maxHours) {
            val isoTime = times[i]
            val hourText = if (i == startIndex) {
                "Bây giờ"
            } else {
                try {
                    isoTime.substringAfter("T").take(5)
                } catch (e: Exception) {
                    isoTime
                }
            }
            val tempVal = temps.getOrNull(i)?.toInt() ?: 28
            val codeVal = codes.getOrNull(i) ?: 0
            val popVal = pops.getOrNull(i) ?: 0
            val isNightHour = try {
                val h = isoTime.substringAfter("T").take(2).toIntOrNull() ?: 12
                h < 6 || h >= 18
            } catch (e: Exception) { false }

            val condition = wmoCodeToCondition(codeVal, isNightHour)
            result.add(HourlyForecast(hourText, tempVal, condition, popVal))
        }
        return result
    }

    private fun parseDailyForecast(daily: DailyDto?): List<DailyForecast> {
        if (daily == null || daily.time.isNullOrEmpty() || daily.minTemperature.isNullOrEmpty() || daily.maxTemperature.isNullOrEmpty()) {
            return emptyList()
        }
        val times = daily.time
        val minTemps = daily.minTemperature
        val maxTemps = daily.maxTemperature
        val codes = daily.weatherCode ?: emptyList()
        val pops = daily.precipitationProbabilityMax ?: emptyList()
        val rainSums = daily.precipitationSum ?: emptyList()
        val windMaxs = daily.windSpeedMax ?: emptyList()
        val uvMaxs = daily.uvIndexMax ?: emptyList()

        val result = mutableListOf<DailyForecast>()
        val daysCount = minOf(times.size, minTemps.size, maxTemps.size)

        val dayFormatter = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val displayFormat = java.text.SimpleDateFormat("EEEE", Locale.forLanguageTag("vi-VN"))

        for (i in 0 until daysCount) {
            val dateStr = times[i]
            val minT = minTemps[i].toInt()
            val maxT = maxTemps[i].toInt()
            val code = codes.getOrNull(i) ?: 0
            val pop = pops.getOrNull(i) ?: 10
            val rainMm = rainSums.getOrNull(i)?.toInt() ?: 0
            val windKmH = (windMaxs.getOrNull(i) ?: 15.0).toInt()
            val uv = uvMaxs.getOrNull(i)?.toInt() ?: 6
            val condition = wmoCodeToCondition(code, false)

            val (dayName, dateText) = when (i) {
                0 -> Pair("Hôm nay", "Hôm nay")
                1 -> Pair("Ngày mai", "Ngày mai")
                else -> {
                    val parsedDate = try { dayFormatter.parse(dateStr) } catch (e: Exception) { null }
                    val name = if (parsedDate != null) displayFormat.format(parsedDate).replaceFirstChar { it.uppercase() } else "Ngày ${i + 1}"
                    Pair(name, "$i ngày tới")
                }
            }

            val summary = when {
                code in listOf(95, 96, 99) -> "Cảnh báo mưa dông kèm sấm sét, gió giật mạnh. Cần chú ý an toàn."
                code in listOf(61, 63, 65, 80, 81, 82) -> "Có mưa rào trong ngày, khả năng mưa ${pop}%. Nên mang theo áo mưa."
                code in listOf(1, 2) -> "Thời tiết đẹp, trời nắng nhẹ có mây, nhiệt độ dao động $minT°C - $maxT°C."
                code == 0 -> "Trời nắng rực rỡ cả ngày, chỉ số UV cao vào giữa trưa."
                else -> "Trời nhiều mây, thời tiết mát mẻ, nhiệt độ từ $minT°C đến $maxT°C."
            }

            result.add(
                DailyForecast(
                    dayName = dayName,
                    dateText = dateText,
                    minTemp = minT,
                    maxTemp = maxT,
                    condition = condition,
                    pop = pop,
                    summary = summary,
                    windSpeedKmH = windKmH,
                    humidityPercent = 70,
                    rainfallMm = rainMm,
                    uvIndex = uv,
                    airQualityIndex = 40
                )
            )
        }
        return result
    }
}

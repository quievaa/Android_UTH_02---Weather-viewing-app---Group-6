package com.example.android_uth_02_weather_viewing_app_group6.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android_uth_02_weather_viewing_app_group6.data.location.LocationTracker
import com.example.android_uth_02_weather_viewing_app_group6.data.model.FavoriteCity
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.AppPreferences
import com.example.android_uth_02_weather_viewing_app_group6.data.repository.WeatherRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.model.CurrentWeather
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.AirQuality
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.CityLocation
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.DailyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.HourlyForecast
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.PrecipitationInfo
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RadarLayer
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RouteWaypoint
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.TripRoute
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.UVIndex
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WeatherCondition
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.WindInfo
import com.example.android_uth_02_weather_viewing_app_group6.data.location.FloodRiskEvaluator
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.UrbanFloodReport
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.ApiConfig
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.model.ApiHealthStatus
import com.example.android_uth_02_weather_viewing_app_group6.data.location.TripRoutingEngine
import com.example.android_uth_02_weather_viewing_app_group6.data.remote.api.RetrofitClient
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiCustomKnowledgeRepository
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.AiWeatherAssistantEngine
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.CustomKnowledgeItem
import com.example.android_uth_02_weather_viewing_app_group6.domain.ai.WeatherIntent
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.ChatMessage
import com.example.android_uth_02_weather_viewing_app_group6.ui.model.RouteGeoPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.example.android_uth_02_weather_viewing_app_group6.data.voice.VoiceWeatherAlertManager
import com.example.android_uth_02_weather_viewing_app_group6.data.voice.VoiceWeatherBulletin
import com.example.android_uth_02_weather_viewing_app_group6.data.voice.WeatherAdvisoryGenerator
import java.util.Locale

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: CurrentWeather) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}

class WeatherViewModel(
    private val repository: WeatherRepository,
    private val appPreferences: AppPreferences? = null,
    private val tripRoutingEngine: TripRoutingEngine = TripRoutingEngine(
        RetrofitClient.osrmApi,
        RetrofitClient.geocodingApi,
        RetrofitClient.weatherApi
    ),
    private val customKnowledgeRepo: AiCustomKnowledgeRepository? = null,
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isCelsius = MutableStateFlow(true)
    val isCelsius: StateFlow<Boolean> = _isCelsius.asStateFlow()

    private val _windUnit = MutableStateFlow("m/s")
    val windUnit: StateFlow<String> = _windUnit.asStateFlow()

    // Multi-API & Resiliency State
    private val _isMultiApiEnabled = MutableStateFlow(true)
    val isMultiApiEnabled: StateFlow<Boolean> = _isMultiApiEnabled.asStateFlow()

    private val _primaryApiProvider = MutableStateFlow("OPEN_METEO")
    val primaryApiProvider: StateFlow<String> = _primaryApiProvider.asStateFlow()

    private val _weatherApiKey = MutableStateFlow(ApiConfig.WEATHERAPI_KEY)
    val weatherApiKey: StateFlow<String> = _weatherApiKey.asStateFlow()

    private val _openWeatherKey = MutableStateFlow(ApiConfig.OPENWEATHER_API_KEY)
    val openWeatherKey: StateFlow<String> = _openWeatherKey.asStateFlow()

    // Custom API Endpoint State (No-Code Configuration)
    private val _customEndpointUrl = MutableStateFlow("")
    val customEndpointUrl: StateFlow<String> = _customEndpointUrl.asStateFlow()

    private val _customEndpointName = MutableStateFlow("Custom API Server")
    val customEndpointName: StateFlow<String> = _customEndpointName.asStateFlow()

    private val _customEndpointEnabled = MutableStateFlow(false)
    val customEndpointEnabled: StateFlow<Boolean> = _customEndpointEnabled.asStateFlow()

    private val _customEndpointKey = MutableStateFlow("")
    val customEndpointKey: StateFlow<String> = _customEndpointKey.asStateFlow()

    private val _customEndpointFormat = MutableStateFlow("OPEN_WEATHER")
    val customEndpointFormat: StateFlow<String> = _customEndpointFormat.asStateFlow()

    private val _customEndpointTestResult = MutableStateFlow<ApiHealthStatus?>(null)
    val customEndpointTestResult: StateFlow<ApiHealthStatus?> = _customEndpointTestResult.asStateFlow()

    private val _isTestingCustomEndpoint = MutableStateFlow(false)
    val isTestingCustomEndpoint: StateFlow<Boolean> = _isTestingCustomEndpoint.asStateFlow()

    private val _apiHealthList = MutableStateFlow<List<ApiHealthStatus>>(emptyList())
    val apiHealthList: StateFlow<List<ApiHealthStatus>> = _apiHealthList.asStateFlow()

    private val _isCheckingApis = MutableStateFlow(false)
    val isCheckingApis: StateFlow<Boolean> = _isCheckingApis.asStateFlow()

    // iOS Style Location State
    private val _isCurrentLocation = MutableStateFlow(false)
    val isCurrentLocation: StateFlow<Boolean> = _isCurrentLocation.asStateFlow()

    private val _locationSubName = MutableStateFlow("")
    val locationSubName: StateFlow<String> = _locationSubName.asStateFlow()

    var hasAutoFetchedLocation: Boolean = false

    val searchHistory: StateFlow<List<String>> = appPreferences?.searchHistory
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf("Ho Chi Minh", "Ha Noi", "Da Nang"),
        ) ?: MutableStateFlow(listOf("Ho Chi Minh", "Ha Noi", "Da Nang")).asStateFlow()

    val favoriteCities: StateFlow<List<FavoriteCity>> = appPreferences?.favoriteCities
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        ) ?: MutableStateFlow<List<FavoriteCity>>(emptyList()).asStateFlow()

    val favoriteCityNames: StateFlow<List<String>> = (appPreferences?.favoriteCities
        ?.map { list -> list.map { it.cityName } })
        ?.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        ) ?: MutableStateFlow<List<String>>(emptyList()).asStateFlow()

    private var lastCity: String = "Ho Chi Minh"
    private var lastCoordinates: Pair<Double, Double>? = null

    // Cảnh báo ngập úng đô thị UTH
    val floodReport: StateFlow<UrbanFloodReport> = _uiState
        .map { state ->
            when (state) {
                is WeatherUiState.Success -> FloodRiskEvaluator.evaluate(state.weather, state.weather.cityName)
                else -> FloodRiskEvaluator.evaluate(null, lastCity)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FloodRiskEvaluator.evaluate(null, "Ho Chi Minh")
        )

    // Radar State (Zoom level 4 to 16 for OpenStreetMap)
    private val _radarZoom = MutableStateFlow(10.0f)
    val radarZoom: StateFlow<Float> = _radarZoom.asStateFlow()

    private val _radarTimelinePosition = MutableStateFlow(0.5f)
    val radarTimelinePosition: StateFlow<Float> = _radarTimelinePosition.asStateFlow()

    private val _isRadarPlaying = MutableStateFlow(false)
    val isRadarPlaying: StateFlow<Boolean> = _isRadarPlaying.asStateFlow()

    private val _selectedRadarLayer = MutableStateFlow(RadarLayer.PRECIPITATION)
    val selectedRadarLayer: StateFlow<RadarLayer> = _selectedRadarLayer.asStateFlow()

    // Voice Weather Alerts & Driver Mode Assistant State
    private var voiceAlertManager: VoiceWeatherAlertManager? = null
    private val _isVoiceSpeaking = MutableStateFlow(false)
    val isVoiceSpeaking: StateFlow<Boolean> = _isVoiceSpeaking.asStateFlow()

    private val _voiceSpeechRate = MutableStateFlow(1.0f)
    val voiceSpeechRate: StateFlow<Float> = _voiceSpeechRate.asStateFlow()

    private val _currentVoiceBulletin = MutableStateFlow<VoiceWeatherBulletin?>(null)
    val currentVoiceBulletin: StateFlow<VoiceWeatherBulletin?> = _currentVoiceBulletin.asStateFlow()

    private val _voiceStatusMessage = MutableStateFlow<String?>(null)
    val voiceStatusMessage: StateFlow<String?> = _voiceStatusMessage.asStateFlow()

    // --- Trợ lý AI Thời tiết UTH ---
    private val aiAssistantEngine = AiWeatherAssistantEngine(customKnowledgeRepo)
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Xin chào! 👋 Tôi là Trợ lý Thời tiết AI thông minh của Trường ĐH Giao thông vận tải TP.HCM (UTH).\n\nTôi đã được huấn luyện hơn 25+ chủ đề thời tiết, ngập úng UTH, tư vấn trang phục, phơi đồ, rửa xe, thể thao và sức khỏe. Bạn cần hỗ trợ gì hôm nay?",
                isUser = false,
                tags = listOf("UTH AI", "Trợ lý ảo")
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _customKnowledgeList = MutableStateFlow<List<CustomKnowledgeItem>>(
        customKnowledgeRepo?.getAll() ?: emptyList()
    )
    val customKnowledgeList: StateFlow<List<CustomKnowledgeItem>> = _customKnowledgeList.asStateFlow()

    private val _currentSuggestionChips = MutableStateFlow<List<String>>(
        listOf("Hôm nay có mưa không?", "Mặc gì hôm nay?", "Đường UTH có ngập không?", "Chỉ số UV & Không khí?", "Có nên phơi đồ không?")
    )
    val currentSuggestionChips: StateFlow<List<String>> = _currentSuggestionChips.asStateFlow()

    fun teachAi(question: String, answer: String) {
        val repo = customKnowledgeRepo ?: return
        val item = repo.addKnowledge(question, answer)
        _customKnowledgeList.value = repo.getAll()
        val noticeMessage = ChatMessage(
            text = "🎓 **Đã nạp kiến thức mới thành công!**\n\n• **Câu hỏi:** \"${item.question}\"\n• **Câu trả lời:** \"${item.answer}\"\n\nTừ bây giờ, khi bạn hỏi câu này hoặc các câu tương tự, tôi sẽ trả lời chính xác theo nội dung bạn đã dạy!",
            isUser = false,
            tags = listOf("Huấn luyện AI")
        )
        _chatMessages.value = _chatMessages.value + noticeMessage
    }

    fun deleteCustomKnowledge(id: String) {
        val repo = customKnowledgeRepo ?: return
        repo.deleteKnowledge(id)
        _customKnowledgeList.value = repo.getAll()
    }

    fun refreshCustomKnowledge() {
        _customKnowledgeList.value = customKnowledgeRepo?.getAll() ?: emptyList()
    }

    fun sendChatMessage(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || _isAiThinking.value) return

        val userMessage = ChatMessage(
            text = trimmed,
            isUser = true
        )
        _chatMessages.value = _chatMessages.value + userMessage
        _isAiThinking.value = true

        viewModelScope.launch {
            // Hiệu ứng suy nghĩ tự nhiên
            delay(400)

            val currentWeather = (_uiState.value as? WeatherUiState.Success)?.weather
            val output = aiAssistantEngine.processQueryWithGemini(
                prompt = trimmed,
                currentWeather = currentWeather,
                floodReport = floodReport.value
            )

            val tags = when {
                output.isCustomKnowledge -> listOf("Kiến thức tùy chỉnh")
                output.isGemini -> listOf("Gemini 3.5 AI", output.detectedIntent.displayName)
                else -> listOf("UTH AI Offline", output.detectedIntent.displayName)
            }

            val aiMessage = ChatMessage(
                text = output.responseText,
                isUser = false,
                tags = tags
            )
            _chatMessages.value = _chatMessages.value + aiMessage
            _currentSuggestionChips.value = output.followUpSuggestions
            _isAiThinking.value = false

        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Cuộc trò chuyện đã được làm mới. Bạn muốn hỏi gì về thời tiết hoặc các tuyến đường UTH?",
                isUser = false,
                tags = listOf("UTH AI")
            )
        )
        _currentSuggestionChips.value = listOf("Hôm nay có mưa không?", "Mặc gì hôm nay?", "Đường UTH có ngập không?", "Chỉ số UV & Không khí?", "Có nên phơi đồ không?")
    }

    // Trip Planner State
    private val initialTrips: List<TripRoute> = listOf(
        TripRoute(
            id = "trip_1",
            origin = "TP. Hồ Chí Minh",
            destination = "Đà Lạt",
            departureTime = "Bây giờ",
            durationText = "6 giờ 15 phút",
            distanceKm = 308,
            hasSevereWarning = true,
            warningTitle = "Cảnh báo sương mù & mưa đèo Bảo Lộc",
            warningDesc = "Tầm nhìn giảm dưới 20m và đường trơn trượt từ km 110 - 135 vào lúc 15:00 - 17:30",
            waypoints = listOf(
                RouteWaypoint(
                    time = "10:00",
                    type = "Khởi hành",
                    locationName = "TP. Hồ Chí Minh",
                    temp = 32,
                    condition = WeatherCondition.SUNNY,
                    windSpeed = "12 km/h",
                    visibility = "10 km",
                    rainAmount = "0 mm",
                    latitude = 10.7769,
                    longitude = 106.7009
                ),
                RouteWaypoint(
                    time = "12:15",
                    type = "Trạm dừng",
                    locationName = "Dầu Giây - Đồng Nai",
                    temp = 30,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    windSpeed = "14 km/h",
                    visibility = "10 km",
                    rainAmount = "0 mm",
                    latitude = 10.9167,
                    longitude = 107.1667
                ),
                RouteWaypoint(
                    time = "14:45",
                    type = "Cảnh báo",
                    locationName = "Đèo Bảo Lộc (Km 120)",
                    temp = 22,
                    condition = WeatherCondition.HEAVY_RAIN,
                    isWarning = true,
                    warningTitle = "Mưa dông & Sương mù dày",
                    warningDesc = "Mặt đường trơn trượt, dốc quanh co nguy hiểm. Giảm tốc độ < 30km/h.",
                    windSpeed = "28 km/h",
                    visibility = "300 m",
                    rainAmount = "24 mm",
                    latitude = 11.4500,
                    longitude = 107.7167
                ),
                RouteWaypoint(
                    time = "16:30",
                    type = "Trạm dừng",
                    locationName = "Đức Trọng",
                    temp = 20,
                    condition = WeatherCondition.LIGHT_RAIN,
                    windSpeed = "16 km/h",
                    visibility = "6 km",
                    rainAmount = "3 mm",
                    latitude = 11.7333,
                    longitude = 108.3667
                ),
                RouteWaypoint(
                    time = "17:30",
                    type = "Điểm đến",
                    locationName = "Đà Lạt (Hồ Xuân Hương)",
                    temp = 17,
                    condition = WeatherCondition.CLOUDY,
                    windSpeed = "15 km/h",
                    visibility = "8 km",
                    rainAmount = "0.5 mm",
                    latitude = 11.9404,
                    longitude = 108.4583
                )
            )
        ),
        TripRoute(
            id = "trip_2",
            origin = "Đà Nẵng",
            destination = "Huế",
            departureTime = "Bây giờ",
            durationText = "2 giờ 10 phút",
            distanceKm = 98,
            hasSevereWarning = false,
            warningTitle = "Thời tiết thuận lợi",
            warningDesc = "Đường khô ráo, gió biển nhẹ dọc Đèo Hải Vân.",
            waypoints = listOf(
                RouteWaypoint(
                    time = "08:00",
                    type = "Khởi hành",
                    locationName = "Đà Nẵng",
                    temp = 29,
                    condition = WeatherCondition.SUNNY,
                    windSpeed = "10 km/h",
                    visibility = "10 km",
                    rainAmount = "0 mm",
                    latitude = 16.0544,
                    longitude = 108.2022
                ),
                RouteWaypoint(
                    time = "08:50",
                    type = "Trạm dừng",
                    locationName = "Đỉnh Đèo Hải Vân",
                    temp = 25,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    windSpeed = "22 km/h",
                    visibility = "10 km",
                    rainAmount = "0 mm",
                    latitude = 16.1878,
                    longitude = 108.1311
                ),
                RouteWaypoint(
                    time = "09:30",
                    type = "Trạm dừng",
                    locationName = "Lăng Cô",
                    temp = 28,
                    condition = WeatherCondition.SUNNY,
                    windSpeed = "14 km/h",
                    visibility = "10 km",
                    rainAmount = "0 mm",
                    latitude = 16.2300,
                    longitude = 108.0100
                ),
                RouteWaypoint(
                    time = "10:15",
                    type = "Điểm đến",
                    locationName = "Cố đô Huế",
                    temp = 29,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    windSpeed = "11 km/h",
                    visibility = "10 km",
                    rainAmount = "0 mm",
                    latitude = 16.4637,
                    longitude = 107.5909
                )
            )
        )
    )

    private val _availableTrips = MutableStateFlow(initialTrips)
    val availableTrips: StateFlow<List<TripRoute>> = _availableTrips.asStateFlow()

    private val _currentTrip = MutableStateFlow(initialTrips.first())
    val currentTrip: StateFlow<TripRoute> = _currentTrip.asStateFlow()

    private val _isPlanningTrip = MutableStateFlow(false)
    val isPlanningTrip: StateFlow<Boolean> = _isPlanningTrip.asStateFlow()

    private val _tripPlanError = MutableStateFlow<String?>(null)
    val tripPlanError: StateFlow<String?> = _tripPlanError.asStateFlow()

    private val _isNavigating = MutableStateFlow(false)
    val isNavigating: StateFlow<Boolean> = _isNavigating.asStateFlow()

    private val _activeWaypointIndex = MutableStateFlow(0)
    val activeWaypointIndex: StateFlow<Int> = _activeWaypointIndex.asStateFlow()

    // Settings state
    private val _roadAlertsEnabled = MutableStateFlow(true)
    val roadAlertsEnabled: StateFlow<Boolean> = _roadAlertsEnabled.asStateFlow()

    private val _selectedVehicle = MutableStateFlow("Ô tô")
    val selectedVehicle: StateFlow<String> = _selectedVehicle.asStateFlow()

    // Forecast expansion
    private val _expandedForecastIndex = MutableStateFlow(0)
    val expandedForecastIndex: StateFlow<Int> = _expandedForecastIndex.asStateFlow()

    val availableCities: List<CityLocation> = listOf(
        CityLocation("hcm", "TP. Hồ Chí Minh", "Việt Nam", 10.76, 106.66, 31, 34, 26, WeatherCondition.SUNNY, "Nắng đẹp"),
        CityLocation("hn", "Hà Nội", "Việt Nam", 21.02, 105.83, 28, 31, 24, WeatherCondition.LIGHT_RAIN, "Mưa nhỏ rải rác"),
        CityLocation("dn", "Đà Nẵng", "Việt Nam", 16.05, 108.20, 30, 33, 25, WeatherCondition.PARTLY_CLOUDY, "Nắng nhẹ"),
        CityLocation("dl", "Đà Lạt", "Việt Nam", 11.94, 108.45, 19, 23, 15, WeatherCondition.CLOUDY, "Se lạnh nhiều mây"),
        CityLocation("nt", "Nha Trang", "Việt Nam", 12.23, 109.19, 31, 33, 26, WeatherCondition.SUNNY, "Nắng rực rỡ")
    )

    init {
        if (appPreferences != null) {
            viewModelScope.launch {
                appPreferences.temperatureUnit.collect { unit ->
                    _isCelsius.value = (unit == "C")
                }
            }
            viewModelScope.launch {
                appPreferences.windUnit.collect { unit ->
                    _windUnit.value = unit
                }
            }
            viewModelScope.launch {
                appPreferences.isMultiApiEnabled.collect { enabled ->
                    _isMultiApiEnabled.value = enabled
                }
            }
            viewModelScope.launch {
                appPreferences.primaryApiProvider.collect { provider ->
                    _primaryApiProvider.value = provider
                }
            }
            viewModelScope.launch {
                appPreferences.weatherApiKey.collect { key ->
                    _weatherApiKey.value = key.ifBlank { ApiConfig.WEATHERAPI_KEY }
                }
            }
            viewModelScope.launch {
                appPreferences.openWeatherKey.collect { key ->
                    _openWeatherKey.value = key.ifBlank { ApiConfig.OPENWEATHER_API_KEY }
                }
            }
            viewModelScope.launch {
                appPreferences.customEndpointUrl.collect { url ->
                    _customEndpointUrl.value = url
                }
            }
            viewModelScope.launch {
                appPreferences.customEndpointName.collect { name ->
                    _customEndpointName.value = name
                }
            }
            viewModelScope.launch {
                appPreferences.customEndpointEnabled.collect { enabled ->
                    _customEndpointEnabled.value = enabled
                }
            }
            viewModelScope.launch {
                appPreferences.customEndpointKey.collect { key ->
                    _customEndpointKey.value = key
                }
            }
            viewModelScope.launch {
                appPreferences.customEndpointFormat.collect { format ->
                    _customEndpointFormat.value = format
                }
            }
        }
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
        _isCurrentLocation.value = false
        _locationSubName.value = ""
        hasAutoFetchedLocation = true

        viewModelScope.launch {
            appPreferences?.addSearchQuery(city)
            _uiState.value = WeatherUiState.Loading
            repository.getCurrentWeather(
                cityName = city,
                isMultiApiEnabled = _isMultiApiEnabled.value,
                primaryProvider = _primaryApiProvider.value,
                customWeatherApiKey = _weatherApiKey.value,
                customOpenWeatherKey = _openWeatherKey.value,
                customEndpointUrl = _customEndpointUrl.value,
                customEndpointName = _customEndpointName.value,
                customEndpointEnabled = _customEndpointEnabled.value,
                customEndpointKey = _customEndpointKey.value,
                customEndpointFormat = _customEndpointFormat.value,
            )
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
            repository.getWeatherByCoordinates(
                latitude = latitude,
                longitude = longitude,
                cityName = cityName,
                isMultiApiEnabled = _isMultiApiEnabled.value,
                primaryProvider = _primaryApiProvider.value,
                customWeatherApiKey = _weatherApiKey.value,
                customOpenWeatherKey = _openWeatherKey.value,
                customEndpointUrl = _customEndpointUrl.value,
                customEndpointName = _customEndpointName.value,
                customEndpointEnabled = _customEndpointEnabled.value,
                customEndpointKey = _customEndpointKey.value,
                customEndpointFormat = _customEndpointFormat.value,
            )
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
        reverseGeocoder: com.example.android_uth_02_weather_viewing_app_group6.data.location.ReverseGeocoder? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> },
    ) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            val location = locationTracker.getCurrentLocation()
            if (location != null) {
                // Resolve detailed district & city name like iOS Apple Weather
                val resolvedPlace = reverseGeocoder?.resolveLocationName(location.latitude, location.longitude)
                    ?: "Vị trí của tôi"
                val displayTitle = "Vị trí của tôi"

                _isCurrentLocation.value = true
                _locationSubName.value = resolvedPlace

                repository.getWeatherByCoordinates(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = displayTitle,
                    isMultiApiEnabled = _isMultiApiEnabled.value,
                    primaryProvider = _primaryApiProvider.value,
                    customWeatherApiKey = _weatherApiKey.value,
                    customOpenWeatherKey = _openWeatherKey.value,
                    customEndpointUrl = _customEndpointUrl.value,
                    customEndpointName = _customEndpointName.value,
                    customEndpointEnabled = _customEndpointEnabled.value,
                    customEndpointKey = _customEndpointKey.value,
                    customEndpointFormat = _customEndpointFormat.value,
                )
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
        val nextIsCelsius = !_isCelsius.value
        _isCelsius.value = nextIsCelsius
        viewModelScope.launch {
            appPreferences?.saveTemperatureUnit(if (nextIsCelsius) "C" else "F")
        }
    }

    fun updateWindUnit(unit: String) {
        _windUnit.value = unit
        viewModelScope.launch {
            appPreferences?.saveWindUnit(unit)
        }
    }

    fun toggleRoadAlerts() {
        _roadAlertsEnabled.value = !_roadAlertsEnabled.value
    }

    fun selectVehicle(vehicle: String) {
        _selectedVehicle.value = vehicle
    }

    fun toggleForecastExpand(index: Int) {
        _expandedForecastIndex.value = if (_expandedForecastIndex.value == index) -1 else index
    }

    // Favorite management
    fun isFavorite(cityName: String): Boolean {
        return favoriteCities.value.any { it.cityName.equals(cityName, ignoreCase = true) }
    }

    fun toggleFavorite(cityName: String, country: String = "Việt Nam", lat: Double = 0.0, lon: Double = 0.0) {
        val existing = favoriteCities.value.firstOrNull { it.cityName.equals(cityName, ignoreCase = true) }
        viewModelScope.launch {
            if (existing != null) {
                appPreferences?.removeFavoriteCity(existing.id)
            } else {
                val id = if (lat != 0.0 && lon != 0.0) "${lat}_${lon}" else cityName.lowercase().replace(" ", "_")
                appPreferences?.addFavoriteCity(
                    FavoriteCity(
                        id = id,
                        cityName = cityName,
                        country = country,
                        latitude = lat,
                        longitude = lon
                    )
                )
            }
        }
    }

    fun toggleFavoriteForCurrentCity() {
        val state = _uiState.value
        if (state is WeatherUiState.Success) {
            val w = state.weather
            toggleFavorite(
                cityName = w.cityName,
                country = "Việt Nam",
                lat = w.latitude ?: 0.0,
                lon = w.longitude ?: 0.0
            )
        }
    }

    fun addFavorite(city: FavoriteCity) {
        viewModelScope.launch {
            appPreferences?.addFavoriteCity(city)
        }
    }

    fun removeFavorite(cityId: String) {
        viewModelScope.launch {
            appPreferences?.removeFavoriteCity(cityId)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            appPreferences?.clearSearchHistory()
        }
    }

    // Radar Controls
    fun toggleRadarPlay() {
        _isRadarPlaying.value = !_isRadarPlaying.value
    }

    fun setRadarTimeline(pos: Float) {
        _radarTimelinePosition.value = pos
    }

    fun selectRadarLayer(layer: RadarLayer) {
        _selectedRadarLayer.value = layer
    }

    fun zoomInRadar() {
        _radarZoom.value = (_radarZoom.value + 1.0f).coerceAtMost(16.0f)
    }

    fun zoomOutRadar() {
        _radarZoom.value = (_radarZoom.value - 1.0f).coerceAtLeast(4.0f)
    }

    // Trip Planner Controls
    fun swapOriginDestination() {
        val trip = _currentTrip.value
        _currentTrip.value = trip.copy(
            origin = trip.destination,
            destination = trip.origin,
            waypoints = trip.waypoints.reversed()
        )
    }

    fun selectTrip(trip: TripRoute) {
        _currentTrip.value = trip
        _isNavigating.value = false
        _activeWaypointIndex.value = 0
    }

    fun startJourney() {
        _isNavigating.value = true
        _activeWaypointIndex.value = 1
    }

    fun stopJourney() {
        _isNavigating.value = false
    }

    val currentUserLocation: RouteGeoPoint?
        get() = lastCoordinates?.let { RouteGeoPoint(it.first, it.second) }
            ?: (uiState.value as? WeatherUiState.Success)?.weather?.let { w ->
                if (w.latitude != null && w.longitude != null) RouteGeoPoint(w.latitude, w.longitude) else null
            }

    /**
     * Lập lộ trình tự do qua OSRM: người dùng nhập điểm đi và điểm đến tùy ý.
     */
    fun planCustomTrip(
        origin: String,
        destination: String,
        vehicleType: String = _selectedVehicle.value,
        departureTime: String = "Bây giờ",
        userLocation: RouteGeoPoint? = null,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            _isPlanningTrip.value = true
            _tripPlanError.value = null

            val effectiveUserLocation = userLocation
                ?: currentUserLocation
                ?: RouteGeoPoint(10.8231, 106.6297)

            val result = tripRoutingEngine.calculateRoute(
                originName = origin,
                destinationName = destination,
                vehicleType = vehicleType,
                departureTimeText = departureTime,
                userLocation = effectiveUserLocation
            )

            _isPlanningTrip.value = false
            result.onSuccess { trip ->
                _currentTrip.value = trip
                _selectedVehicle.value = vehicleType
                _isNavigating.value = false
                _activeWaypointIndex.value = 0

                val currentList = _availableTrips.value.toMutableList()
                if (currentList.none { it.origin.equals(trip.origin, true) && it.destination.equals(trip.destination, true) }) {
                    currentList.add(0, trip)
                    _availableTrips.value = currentList
                }
                onComplete(true, null)
            }.onFailure { err ->
                val msg = err.message ?: "Không thể tạo lộ trình. Vui lòng kiểm tra lại địa danh."
                _tripPlanError.value = msg
                onComplete(false, msg)
            }
        }
    }

    fun clearTripPlanError() {
        _tripPlanError.value = null
    }

    // Helper functions to generate hourly and 10-day forecast with real API data fallback
    fun getHourlyForecastList(
        weather: CurrentWeather? = null,
        baseTemp: Double = 30.0,
        baseCondition: String = "Nắng đẹp"
    ): List<HourlyForecast> {
        if (weather != null && weather.hourlyForecast.isNotEmpty()) {
            return weather.hourlyForecast
        }
        val cond = WeatherCondition.fromDescription(baseCondition)
        val tempInt = baseTemp.toInt()
        return listOf(
            HourlyForecast("Bây giờ", tempInt, cond, 10),
            HourlyForecast("11:00", tempInt + 1, cond, 10),
            HourlyForecast("12:00", tempInt + 2, cond, 15),
            HourlyForecast("13:00", tempInt + 3, WeatherCondition.SUNNY, 5),
            HourlyForecast("14:00", tempInt + 2, WeatherCondition.PARTLY_CLOUDY, 20),
            HourlyForecast("15:00", tempInt + 1, WeatherCondition.LIGHT_RAIN, 60),
            HourlyForecast("16:00", tempInt, WeatherCondition.RAIN, 75),
            HourlyForecast("17:00", tempInt - 1, WeatherCondition.LIGHT_RAIN, 40),
            HourlyForecast("18:00", tempInt - 2, WeatherCondition.CLOUDY, 20),
            HourlyForecast("19:00", tempInt - 3, WeatherCondition.NIGHT_CLOUDY, 10),
            HourlyForecast("20:00", tempInt - 3, WeatherCondition.NIGHT_CLEAR, 5),
            HourlyForecast("21:00", tempInt - 4, WeatherCondition.NIGHT_CLEAR, 0)
        )
    }

    fun getHourlyForecastList(baseTemp: Double, baseCondition: String): List<HourlyForecast> =
        getHourlyForecastList(null, baseTemp, baseCondition)

    fun getTenDayForecastList(
        weather: CurrentWeather? = null,
        baseTemp: Double = 30.0,
        baseCondition: String = "Nắng đẹp"
    ): List<DailyForecast> {
        if (weather != null && weather.dailyForecast.isNotEmpty()) {
            return weather.dailyForecast
        }
        val cond = WeatherCondition.fromDescription(baseCondition)
        val t = baseTemp.toInt()
        return listOf(
            DailyForecast(
                dayName = "Hôm nay",
                dateText = "Hôm nay",
                minTemp = t - 5,
                maxTemp = t + 2,
                condition = cond,
                pop = 35,
                summary = "Ngày có nắng nhẹ, chiều tối có khả năng xuất hiện mưa rào rải rác vài nơi.",
                windSpeedKmH = 14,
                humidityPercent = 72,
                rainfallMm = 4,
                uvIndex = 7,
                airQualityIndex = 42
            ),
            DailyForecast(
                dayName = "Thứ Ba",
                dateText = "Ngày mai",
                minTemp = t - 6,
                maxTemp = t + 1,
                condition = WeatherCondition.LIGHT_RAIN,
                pop = 65,
                summary = "Mưa rào nhẹ vào đầu giờ chiều, nhiệt độ dịu mát, độ ẩm cao.",
                windSpeedKmH = 16,
                humidityPercent = 80,
                rainfallMm = 8,
                uvIndex = 5,
                airQualityIndex = 35
            ),
            DailyForecast(
                dayName = "Thứ Tư",
                dateText = "Ngày kia",
                minTemp = t - 4,
                maxTemp = t + 3,
                condition = WeatherCondition.SUNNY,
                pop = 10,
                summary = "Trời quang mây, nắng rực rỡ cả ngày, chỉ số UV cao vào giữa trưa.",
                windSpeedKmH = 12,
                humidityPercent = 65,
                rainfallMm = 0,
                uvIndex = 9,
                airQualityIndex = 55
            ),
            DailyForecast(
                dayName = "Thứ Năm",
                dateText = "3 ngày tới",
                minTemp = t - 7,
                maxTemp = t,
                condition = WeatherCondition.THUNDERSTORM,
                pop = 85,
                summary = "Cảnh báo mưa dông kèm gió giật mạnh vào cuối buổi chiều. Đề phòng ngập úng.",
                windSpeedKmH = 28,
                humidityPercent = 88,
                rainfallMm = 28,
                uvIndex = 4,
                airQualityIndex = 30
            ),
            DailyForecast(
                dayName = "Thứ Sáu",
                dateText = "4 ngày tới",
                minTemp = t - 5,
                maxTemp = t + 1,
                condition = WeatherCondition.PARTLY_CLOUDY,
                pop = 25,
                summary = "Thời tiết ổn định trở lại, trời nhiều mây xen kẽ nắng nhẹ.",
                windSpeedKmH = 15,
                humidityPercent = 70,
                rainfallMm = 2,
                uvIndex = 6,
                airQualityIndex = 40
            ),
            DailyForecast(
                dayName = "Thứ Bảy",
                dateText = "Cuối tuần",
                minTemp = t - 4,
                maxTemp = t + 2,
                condition = WeatherCondition.SUNNY,
                pop = 15,
                summary = "Cuối tuần lý tưởng cho các hoạt động dã ngoại ngoài trời, trời nắng đẹp.",
                windSpeedKmH = 11,
                humidityPercent = 64,
                rainfallMm = 0,
                uvIndex = 8,
                airQualityIndex = 48
            ),
            DailyForecast(
                dayName = "Chủ Nhật",
                dateText = "Cuối tuần",
                minTemp = t - 5,
                maxTemp = t + 1,
                condition = WeatherCondition.CLOUDY,
                pop = 30,
                summary = "Trời nhiều mây, chiều tối có gió mùa nhẹ mát mẻ.",
                windSpeedKmH = 18,
                humidityPercent = 74,
                rainfallMm = 1,
                uvIndex = 6,
                airQualityIndex = 45
            )
        )
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

    fun toggleMultiApi() {
        val next = !_isMultiApiEnabled.value
        _isMultiApiEnabled.value = next
        viewModelScope.launch {
            appPreferences?.saveMultiApiEnabled(next)
        }
    }

    fun setPrimaryApiProvider(provider: String) {
        _primaryApiProvider.value = provider
        viewModelScope.launch {
            appPreferences?.savePrimaryApiProvider(provider)
        }
    }

    fun saveCustomApiKeys(weatherKey: String, openWeatherKey: String) {
        _weatherApiKey.value = weatherKey.trim()
        _openWeatherKey.value = openWeatherKey.trim()
        viewModelScope.launch {
            appPreferences?.saveWeatherApiKey(weatherKey.trim())
            appPreferences?.saveOpenWeatherKey(openWeatherKey.trim())
        }
    }

    fun saveCustomEndpoint(
        url: String,
        name: String,
        enabled: Boolean,
        apiKey: String,
        format: String
    ) {
        val trimmedUrl = url.trim()
        val trimmedName = name.trim().ifBlank { "Custom API" }
        _customEndpointUrl.value = trimmedUrl
        _customEndpointName.value = trimmedName
        _customEndpointEnabled.value = enabled
        _customEndpointKey.value = apiKey.trim()
        _customEndpointFormat.value = format

        if (enabled && trimmedUrl.isNotBlank()) {
            _primaryApiProvider.value = "CUSTOM_ENDPOINT"
        } else if (_primaryApiProvider.value == "CUSTOM_ENDPOINT") {
            _primaryApiProvider.value = "OPEN_METEO"
        }

        viewModelScope.launch {
            appPreferences?.saveCustomEndpoint(trimmedUrl, trimmedName, enabled, apiKey.trim(), format)
            if (enabled && trimmedUrl.isNotBlank()) {
                appPreferences?.savePrimaryApiProvider("CUSTOM_ENDPOINT")
            }
        }
    }

    fun testCustomEndpoint(
        url: String,
        name: String,
        apiKey: String,
        format: String
    ) {
        viewModelScope.launch {
            _isTestingCustomEndpoint.value = true
            try {
                val res = repository.pingCustomEndpoint(
                    url = url.trim(),
                    name = name.trim().ifBlank { "Custom API" },
                    apiKey = apiKey.trim(),
                    format = format
                )
                _customEndpointTestResult.value = res
            } catch (e: Exception) {
                _customEndpointTestResult.value = ApiHealthStatus(
                    providerName = name,
                    isOnline = false,
                    latencyMs = 0,
                    message = e.localizedMessage ?: "Lỗi kết nối"
                )
            } finally {
                _isTestingCustomEndpoint.value = false
            }
        }
    }

    fun resetCustomEndpoint() {
        _customEndpointUrl.value = ""
        _customEndpointName.value = "Custom API Server"
        _customEndpointEnabled.value = false
        _customEndpointKey.value = ""
        _customEndpointFormat.value = "OPEN_WEATHER"
        _customEndpointTestResult.value = null
        if (_primaryApiProvider.value == "CUSTOM_ENDPOINT") {
            _primaryApiProvider.value = "OPEN_METEO"
        }

        viewModelScope.launch {
            appPreferences?.resetCustomEndpoint()
            appPreferences?.savePrimaryApiProvider("OPEN_METEO")
        }
    }

    fun testAllApisHealth() {
        viewModelScope.launch {
            _isCheckingApis.value = true
            try {
                val results = repository.pingAllApis(
                    customWeatherApiKey = _weatherApiKey.value,
                    customOpenWeatherKey = _openWeatherKey.value,
                    customEndpointUrl = _customEndpointUrl.value,
                    customEndpointName = _customEndpointName.value,
                    customEndpointEnabled = _customEndpointEnabled.value,
                    customEndpointKey = _customEndpointKey.value,
                    customEndpointFormat = _customEndpointFormat.value
                ).map { status ->
                    val isPrim = when (status.providerName) {
                        "Open-Meteo" -> _primaryApiProvider.value == "OPEN_METEO"
                        "WeatherAPI.com" -> _primaryApiProvider.value == "WEATHER_API"
                        "OpenWeatherMap" -> _primaryApiProvider.value == "OPEN_WEATHER"
                        _customEndpointName.value -> _primaryApiProvider.value == "CUSTOM_ENDPOINT"
                        else -> false
                    }
                    status.copy(isPrimary = isPrim)
                }
                _apiHealthList.value = results
            } catch (e: Exception) {
                // Keep existing or empty
            } finally {
                _isCheckingApis.value = false
            }
        }
    }

    fun retry() {
        val coords = lastCoordinates
        viewModelScope.launch {
            _isRefreshing.value = true
            if (coords != null) {
                repository.getWeatherByCoordinates(
                    latitude = coords.first,
                    longitude = coords.second,
                    isMultiApiEnabled = _isMultiApiEnabled.value,
                    primaryProvider = _primaryApiProvider.value,
                    customWeatherApiKey = _weatherApiKey.value,
                    customOpenWeatherKey = _openWeatherKey.value,
                    customEndpointUrl = _customEndpointUrl.value,
                    customEndpointName = _customEndpointName.value,
                    customEndpointEnabled = _customEndpointEnabled.value,
                    customEndpointKey = _customEndpointKey.value,
                    customEndpointFormat = _customEndpointFormat.value,
                )
                    .onSuccess {
                        lastCity = it.cityName
                        _uiState.value = WeatherUiState.Success(it)
                    }
                    .onFailure {
                        _uiState.value = WeatherUiState.Error(
                            it.message ?: "Đã xảy ra lỗi khi tải dữ liệu từ vị trí GPS."
                        )
                    }
            } else {
                repository.getCurrentWeather(
                    cityName = lastCity,
                    isMultiApiEnabled = _isMultiApiEnabled.value,
                    primaryProvider = _primaryApiProvider.value,
                    customWeatherApiKey = _weatherApiKey.value,
                    customOpenWeatherKey = _openWeatherKey.value,
                    customEndpointUrl = _customEndpointUrl.value,
                    customEndpointName = _customEndpointName.value,
                    customEndpointEnabled = _customEndpointEnabled.value,
                    customEndpointKey = _customEndpointKey.value,
                    customEndpointFormat = _customEndpointFormat.value,
                )
                    .onSuccess { _uiState.value = WeatherUiState.Success(it) }
                    .onFailure {
                        _uiState.value = WeatherUiState.Error(
                            it.message ?: "Đã xảy ra lỗi khi tải dữ liệu thời tiết."
                        )
                    }
            }
            _isRefreshing.value = false
        }
    }

    fun toggleFavorite(cityName: String) {
        viewModelScope.launch {
            val favorites = favoriteCities.value
            val existing = favorites.find { it.cityName.equals(cityName, ignoreCase = true) }
            if (existing != null) {
                appPreferences?.removeFavoriteCity(existing.id)
            } else {
                appPreferences?.addFavoriteCity(
                    FavoriteCity(
                        id = cityName.lowercase(),
                        cityName = cityName,
                        country = "Vietnam",
                        latitude = lastCoordinates?.first ?: 10.8231,
                        longitude = lastCoordinates?.second ?: 106.6297
                    )
                )
            }
        }
    }

    fun playVoiceAdvisory(context: Context, customSpeechRate: Float? = null) {
        val currentWeather = when (val state = _uiState.value) {
            is WeatherUiState.Success -> state.weather
            else -> CurrentWeather(
                cityName = lastCity,
                temperatureC = 30.0,
                feelsLikeC = 32.0,
                minTemperatureC = 25.0,
                maxTemperatureC = 33.0,
                description = "Thời tiết bình thường",
                weatherMain = "Clear",
                humidityPercent = 70,
                pressureHpa = 1012.0,
                windSpeedMps = 3.5,
                windDirectionDeg = null,
                latitude = lastCoordinates?.first,
                longitude = lastCoordinates?.second,
                iconCode = "01d"
            )
        }

        val bulletin = WeatherAdvisoryGenerator.generateBulletin(currentWeather)
        _currentVoiceBulletin.value = bulletin

        val rate = customSpeechRate ?: _voiceSpeechRate.value
        _voiceSpeechRate.value = rate

        if (voiceAlertManager == null) {
            voiceAlertManager = VoiceWeatherAlertManager(context) { success, errorMsg ->
                if (!success && errorMsg != null) {
                    _voiceStatusMessage.value = errorMsg
                    _isVoiceSpeaking.value = false
                }
            }
        }

        voiceAlertManager?.speak(
            text = bulletin.fullSpeechScript,
            speechRate = rate,
            onStart = {
                _isVoiceSpeaking.value = true
                _voiceStatusMessage.value = null
            },
            onDone = {
                _isVoiceSpeaking.value = false
            },
            onError = { errMsg ->
                _isVoiceSpeaking.value = false
                _voiceStatusMessage.value = errMsg
            }
        )
    }

    fun stopVoiceAdvisory() {
        voiceAlertManager?.stop()
        _isVoiceSpeaking.value = false
    }

    fun setVoiceSpeechRate(rate: Float, context: Context? = null) {
        _voiceSpeechRate.value = rate
        voiceAlertManager?.setSpeechRate(rate)
        if (_isVoiceSpeaking.value && context != null) {
            playVoiceAdvisory(context, rate)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceAlertManager?.shutdown()
        voiceAlertManager = null
    }
}